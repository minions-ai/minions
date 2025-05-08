package com.minionslab.mcp.step;

import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.model.MCPModelCallResponse;
import com.minionslab.mcp.model.ModelCallExecutorFactory;
import com.minionslab.mcp.tool.MCPToolCall;
import com.minionslab.mcp.tool.ToolCallExecutorFactory;
import com.minionslab.mcp.tool.ToolCallStatus;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * StepExecutor orchestrates the execution of a single Step.
 *
 * <p>Execution Flow:</p>
 * <ol>
 *   <li>Initial model call is made using the LLM.</li>
 *   <li>If the model call returns tool calls, they are executed in parallel by default, or sequentially if specified in MCPContext.</li>
 *   <li>Tool call results are sent back to the LLM as a follow-up model call.</li>
 *   <li>This loop (model call -> tool calls -> model call) repeats until either:</li>
 *   <ul>
 *     <li>The LLM calls the <b>final_Answer</b> tool, or</li>
 *     <li>The maximum number of model calls per step (from MCPContext) is reached.</li>
 *   </ul>
 * </ol>
 *
 * <p>Tracking:</p>
 * <ul>
 *   <li>All model calls and tool calls are tracked chronologically in separate lists in StepExecution.</li>
 *   <li>Each call records its input, output, status, and timestamp.</li>
 * </ul>
 *
 * <p>Error Handling:</p>
 * <ul>
 *   <li>Tool call retries are governed by configuration (global/agent/step, most specific wins).</li>
 *   <li>If a tool call fails after all retries, an error message is sent to the LLM as a model call.</li>
 * </ul>
 *
 * <p>Configuration:</p>
 * <ul>
 *   <li>Maximum model calls per step and tool call retry limits are read from MCPContext (with sensible defaults).</li>
 *   <li>Tool call execution is parallel by default, but can be set to sequential via MCPContext.</li>
 * </ul>
 *
 * <p>API:</p>
 * <ul>
 *   <li>The public API remains unchanged: <code>execute()</code> returns a <code>CompletableFuture&lt;StepExecution&gt;</code>.</li>
 * </ul>
 */
@Slf4j
public class StepExecutor {
    private final Step step;
    
    private final Executor executor;
    private final MCPContext context;
    private final int maxModelCalls;
    private final int maxToolRetries;
    private final boolean sequentialToolCalls;
    private final StepManager stepManager;
    
    private ModelCallExecutorFactory modelCallExecutorFactory;
    private ToolCallExecutorFactory toolCallExecutorFactory;
    
    public StepExecutor(Step step, MCPContext context, ModelCallExecutorFactory modelCallExecutorFactory, ToolCallExecutorFactory toolCallExecutorFactory) {
        this(step, context, ForkJoinPool.commonPool(), modelCallExecutorFactory, toolCallExecutorFactory);
    }
    
    public StepExecutor(Step step, MCPContext context, Executor executor, ModelCallExecutorFactory modelCallExecutorFactory, ToolCallExecutorFactory toolCallExecutorFactory) {
        this.step = step;
        this.context = context;
        
        this.executor = executor;
        maxModelCalls = context.getMetadata().getOrDefault("maxModelCallsPerStep", 10) instanceof Integer ?
                                (Integer) context.getMetadata().get("maxModelCallsPerStep") : 10;
        maxToolRetries = context.getMetadata().getOrDefault("maxToolCallRetries", 2) instanceof Integer ?
                                 (Integer) context.getMetadata().get("maxToolCallRetries") : 2;
        sequentialToolCalls = Boolean.TRUE.equals(context.getMetadata().get("sequentialToolCalls"));
        this.modelCallExecutorFactory = modelCallExecutorFactory;
        this.toolCallExecutorFactory = toolCallExecutorFactory;
        this.stepManager = context.getStepManager();
        
    }
    
    /**
     * Executes the step asynchronously, managing model and tool calls as per the documented flow.
     *
     * @return A future containing the step execution
     */
    public CompletableFuture<StepExecution> execute() {
        
        log.info("Starting execution for step: {}", step != null ? step.getId() : "null");
        return CompletableFuture.supplyAsync(this::doExecute, executor);
    }
    
    /**
     * Orchestrates the execution of a single step, including model calls, tool calls, instruction handling,
     * and step completion logic. Uses helper methods for each logical block for clarity and maintainability.
     *
     * @return The completed StepExecution
     */
    @NotNull
    StepExecution doExecute() {
        Step step = this.step;
        if (step == null) {
            throw new IllegalStateException("No current step set in context");
        }
        StepExecution execution = prepareStepExecution(step);
        AtomicInteger modelCallCount = new AtomicInteger(0);
        AtomicBoolean stepComplete = new AtomicBoolean(false);
        try {
            MCPModelCall currentModelCall = step.createInitialModelCall();
            execution.getModelCalls().add(currentModelCall);
            modelCallCount.incrementAndGet();
            log.debug("Initial model call created for step: {}", step.getId());
            while (!stepComplete.get() && modelCallCount.get() <= maxModelCalls) {
                MCPModelCallResponse mcpModelCallResponse = executeModelCall(currentModelCall, step, modelCallCount);
                Step.StepInstruction instruction = mcpModelCallResponse.getInstruction();
                if (instruction != null) {
                    
                    
                    boolean completed = false;
                    Step.StepOutcome outcome = instruction.outcome();
                    // Track the instruction in StepManager
                    stepManager.addInstruction(instruction);
                    switch (outcome) {
                        case COMPLETED -> {
                            step.getStepExecution().complete();
                            if (instruction.nextStepSuggestion() != null) {
                                stepManager.setCurrentStepById(instruction.nextStepSuggestion());
                            }
                            completed = true;
                        }
                        case CAN_NOT_FINISH -> step.getStepExecution().fail(instruction.reason());
                        case UNRECOVERABLE_ERROR -> step.getStepExecution().fail(instruction.reason());
                        case SKIPPED -> {
                            step.getStepExecution().skip();
                            if (instruction.nextStepSuggestion() != null) {
                                stepManager.setCurrentStepById(instruction.nextStepSuggestion());
                            }
                            completed = true;
                        }
                        case AWAITING_TOOL_RESULTS, CONTINUE -> {
                            // Do not mark as complete; continue execution
                        }
                    }
                    stepManager.removeInstructionToExecute(instruction);
                    stepComplete.set(completed);
                } else {
                    stepComplete.set(handleInstructionsAndCheckComplete(context, mcpModelCallResponse, execution));
                }
                // Handle tool call requests from the response
                List<MCPToolCall> toolCalls = mcpModelCallResponse.getToolCalls();
                log.debug("Model call #{} for step: {} returned {} tool calls", modelCallCount.get(), step.getId(), toolCalls.size());
                List<MCPToolCall> executedToolCalls = executeToolCalls(toolCalls, step);
                execution.getToolCalls().addAll(executedToolCalls);
                if (!stepComplete.get()) {
                    currentModelCall = handleFollowUpModelCall(step, currentModelCall, executedToolCalls, execution, modelCallCount);
                }
            }
            finalizeStepExecution(execution, step, modelCallCount, stepComplete);
        } catch (Exception e) {
            handleStepExecutionError(e, execution, step);
        }
        return execution;
    }
    
    /**
     * Prepares a new StepExecution for the given step.
     */
    private StepExecution prepareStepExecution(Step step) {
        StepExecution execution = new StepExecution(step, step.getCompletionCriteria());
        step.setStepExecution(execution);
        execution.setModelCalls(new ArrayList<>());
        execution.setToolCalls(new ArrayList<>());
        return execution;
    }
    
    /**
     * Executes a model call for the step and increments the model call count.
     *
     * @return
     */
    private MCPModelCallResponse executeModelCall(MCPModelCall modelCall, Step step, AtomicInteger modelCallCount) {
        log.info("Executing model call #{} for step: {}", modelCallCount.get(), step.getId());
        String provider = context.getModelConfig() != null ? context.getModelConfig().getProvider() : "spring";
        return modelCallExecutorFactory.forProvider(provider, modelCall, context).execute().join();
    }
    
    /**
     * Executes all tool calls for the step, either sequentially or in parallel, and returns the executed tool calls.
     */
    private List<MCPToolCall> executeToolCalls(List<MCPToolCall> toolCalls, Step step) {
        List<MCPToolCall> executedToolCalls = new ArrayList<>();
        if (sequentialToolCalls) {
            for (MCPToolCall toolCall : toolCalls) {
                log.info("Executing tool call {} (sequential) for step: {}", toolCall.getName(), step.getId());
                executeToolCallWithRetries(toolCall, maxToolRetries);
                executedToolCalls.add(toolCall);
            }
        } else {
            List<CompletableFuture<Void>> futures = toolCalls.stream()
                                                             .map(tc -> CompletableFuture.runAsync(() -> {
                                                                 log.info("Executing tool call {} (parallel) for step: {}", tc.getName(), step.getId());
                                                                 executeToolCallWithRetries(tc, maxToolRetries);
                                                             }, executor))
                                                             .toList();
            futures.forEach(CompletableFuture::join);
            executedToolCalls.addAll(toolCalls);
        }
        return executedToolCalls;
    }
    
    private void executeToolCallWithRetries(MCPToolCall toolCall, int maxRetries) {
        int attempt = 0;
        boolean success = false;
        String provider = context.getModelConfig() != null ? context.getModelConfig().getProvider() : "spring";
        while (attempt <= maxRetries && !success) {
            try {
                log.debug("Attempt {} for tool call {} in step {}", attempt + 1, toolCall.getName(), step.getId());
                toolCallExecutorFactory.forProvider(provider, toolCall, context).execute().join();
                if (toolCall.getStatus() == ToolCallStatus.COMPLETED) {
                    success = true;
                    log.info("Tool call {} completed successfully in step {} on attempt {}", toolCall.getName(), step.getId(), attempt + 1);
                } else {
                    attempt++;
                    log.warn("Tool call {} did not complete successfully in step {} on attempt {}", toolCall.getName(), step.getId(), attempt);
                }
            } catch (Exception e) {
                log.error("Tool call execution failed (attempt {}) for tool call {} in step {}: {}", attempt + 1, toolCall.getName(), step.getId(), e.getMessage(), e);
                attempt++;
            }
        }
        if (!success) {
            log.error("Tool call {} failed after {} attempts in step {}", toolCall.getName(), maxRetries + 1, step.getId());
        }
    }
    
    /**
     * Handles the creation and execution of a follow-up model call after tool calls, if the step is not complete.
     */
    private MCPModelCall handleFollowUpModelCall(Step step, MCPModelCall currentModelCall, List<MCPToolCall> executedToolCalls, StepExecution execution,
                                                 AtomicInteger modelCallCount) {
        MCPModelCall followUpModelCall = step.createFollowUpModelCall(currentModelCall, executedToolCalls);
        execution.getModelCalls().add(followUpModelCall);
        modelCallCount.incrementAndGet();
        return followUpModelCall;
    }
    
    /**
     * Finalizes the step execution by setting status, error, and advancing the current step in the context.
     */
    private void finalizeStepExecution(StepExecution execution, Step step, AtomicInteger modelCallCount, AtomicBoolean stepComplete) {
        if (modelCallCount.get() > maxModelCalls) {
            execution.fail("Maximum number of model calls per step exceeded");
            log.error("Step {} failed: maximum number of model calls per step exceeded", step.getId());
        } else if (stepComplete.get()) {
            execution.complete();
            log.info("Step {} completed successfully", step.getId());
        } else {
            execution.complete();
            log.info("Step {} completed (default path)", step.getId());
        }
        // After completion, set currentStep to the next step if any
        List<Step> possibleNextSteps = context.getStepManager().getPossibleNextSteps();
        if (possibleNextSteps.size() == 1) {
            context.getStepManager().setCurrentStep(possibleNextSteps.get(0));
        } else {
            context.getStepManager().setCurrentStepToNull(); // Let agent logic or model choose next
        }
    }
    
    /**
     * Handles errors during step execution by logging and updating the execution status and error.
     */
    private void handleStepExecutionError(Exception e, StepExecution execution, Step step) {
        log.error("Step execution failed for step {}: {}", step.getId(), e.getMessage(), e);
        execution.fail(e.getMessage());
    }
    
    /**
     * Handles LLM instructions and returns true if the step should be considered complete.
     */
    private boolean handleInstructionsAndCheckComplete(MCPContext context, MCPModelCallResponse mcpModelCallResponse, StepExecution execution) {
        List<Step.StepInstruction> instructions = context.getStepManager().getInstructionsToExecute();
        boolean completed = false;
        for (Step.StepInstruction instruction : new ArrayList<>(instructions)) { // avoid concurrent modification
            Step currentStep = context.getStepManager().getCurrentStep();
            Step.StepOutcome outcome = instruction.outcome();
            // Track the instruction in StepManager
            context.getStepManager().addInstruction(instruction);
            switch (outcome) {
                case COMPLETED -> {
                    currentStep.getStepExecution().complete();
                    if (instruction.nextStepSuggestion() != null) {
                        context.getStepManager().setCurrentStepById(instruction.nextStepSuggestion());
                    }
                    completed = true;
                }
                case CAN_NOT_FINISH -> currentStep.getStepExecution().fail(instruction.reason());
                case UNRECOVERABLE_ERROR -> currentStep.getStepExecution().fail(instruction.reason());
                case SKIPPED -> {
                    currentStep.getStepExecution().skip();
                    if (instruction.nextStepSuggestion() != null) {
                        context.getStepManager().setCurrentStepById(instruction.nextStepSuggestion());
                    }
                    completed = true;
                }
                case AWAITING_TOOL_RESULTS, CONTINUE -> {
                    // Do not mark as complete; continue execution
                }
            }
            context.getStepManager().removeInstructionToExecute(instruction);
        }
        return completed;
    }
}
 