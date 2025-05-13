package com.minionslab.core.step;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.message.Message;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.model.ModelCallExecutorFactory;
import com.minionslab.core.model.ModelCallResponse;
import com.minionslab.core.model.OutputInstructions;
import com.minionslab.core.step.completion.DefaultStepCompletionChain;
import com.minionslab.core.step.completion.StepCompletionResult;
import com.minionslab.core.tool.ToolCall;
import com.minionslab.core.tool.ToolCallExecutorFactory;
import com.minionslab.core.tool.ToolCallStatus;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * StepExecutor orchestrates the execution of a single Step.
 *
 * <p>Execution Flow:</p>
 * <ol>
 *   <li>Initial model call is made using the LLM.</li>
 *   <li>If the model call returns tool calls, they are executed in parallel by default, or sequentially if specified in AgentContext.</li>
 *   <li>Tool call results are sent back to the LLM as a follow-up model call.</li>
 *   <li>This loop (model call -> tool calls -> model call) repeats until either:</li>
 *   <ul>
 *     <li>The LLM calls the <b>final_Answer</b> tool, or</li>
 *     <li>The maximum number of model calls per step (from AgentContext) is reached.</li>
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
 *   <li>Maximum model calls per step and tool call retry limits are read from AgentContext (with sensible defaults).</li>
 *   <li>Tool call execution is parallel by default, but can be set to sequential via AgentContext.</li>
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
    private final AgentContext context;
    private final int maxModelCalls;
    private final int maxToolRetries;
    private final boolean sequentialToolCalls;
    private final StepManager stepManager;
    private final @NotNull DefaultStepCompletionChain completionChain;
    private final ModelCallExecutorFactory modelCallExecutorFactory;
    private final ToolCallExecutorFactory toolCallExecutorFactory;

    /**
     * Main constructor for StepExecutor.
     */
    public StepExecutor(Step step, AgentContext context, Executor executor, ModelCallExecutorFactory modelCallExecutorFactory, ToolCallExecutorFactory toolCallExecutorFactory) {
        this.step = step;
        this.context = context;
        this.executor = executor;
        this.maxModelCalls = context.getMetadata().getOrDefault("maxModelCallsPerStep", 10) instanceof Integer ?
                (Integer) context.getMetadata().get("maxModelCallsPerStep") : 10;
        this.maxToolRetries = context.getMetadata().getOrDefault("maxToolCallRetries", 2) instanceof Integer ?
                (Integer) context.getMetadata().get("maxToolCallRetries") : 2;
        this.sequentialToolCalls = Boolean.TRUE.equals(context.getMetadata().get("sequentialToolCalls"));
        this.completionChain = new DefaultStepCompletionChain(maxModelCalls);
        this.modelCallExecutorFactory = modelCallExecutorFactory;
        this.toolCallExecutorFactory = toolCallExecutorFactory;
        this.stepManager = context.getStepManager();
    }

    /**
     * Convenience constructor using ForkJoinPool.commonPool().
     */
    public StepExecutor(Step step, AgentContext context, ModelCallExecutorFactory modelCallExecutorFactory, ToolCallExecutorFactory toolCallExecutorFactory) {
        this(step, context, ForkJoinPool.commonPool(), modelCallExecutorFactory, toolCallExecutorFactory);
    }

    /**
     * Executes the step asynchronously, managing model and tool calls as per the documented flow.
     *
     * @return A future containing the step execution
     */
    public CompletableFuture<StepExecution> executeAsync() {
        log.info("Starting execution for step: {}", step != null ? step.getId() : "null");
        return CompletableFuture.supplyAsync(this::execute, executor);
    }

    /**
     * Orchestrates the execution of a single step, including model calls, tool calls, instruction handling,
     * and step completion logic. Uses helper methods for each logical block for clarity and maintainability.
     *
     * @return The completed StepExecution
     */
    @NotNull
    public StepExecution execute() {
        StepExecution execution = prepareStepExecution(step);
        AtomicInteger modelCallCount = new AtomicInteger(0);
        try {
            boolean stepComplete;
            do {
                ModelCall currentModelCall = buildModelCall();
                execution.getModelCalls().add(currentModelCall);
                modelCallCount.incrementAndGet();
                log.debug("Initial model call created for step: {}", step.getId());
                ModelCallResponse modelCallResponse = executeModelCallWithRetry(currentModelCall, modelCallCount);
                handleToolCalls(modelCallResponse, execution);
                StepCompletionResult completionResult = completionChain.isComplete(execution);
                stepComplete = completionResult != StepCompletionResult.PASS;
            } while (!stepComplete && modelCallCount.get() < maxModelCalls);
            finalizeStepExecution(execution);
        } catch (Exception e) {
            handleStepExecutionError(e, execution);
        }
        return execution;
    }

    // --- Model Call Logic ---

    /**
     * Builds a ModelCall for the current step, including prompt messages and output instructions.
     */
    private @NotNull ModelCall buildModelCall() {
        List<Message> promptMessages = context.getChatMemory().getPromptMessages(context.getConversationid());
        List<Message> allMessages = new ArrayList<>(promptMessages);
        if (step instanceof DefaultStep ds) {
            if (ds.getMessageBundle() != null && ds.getMessageBundle().getAllMessages() != null) {
                allMessages.addAll(ds.getMessageBundle().getAllMessages());
            }
        } else {
            Message goal = step.getGoal();
            Message systemPrompt = step.getSystemPrompt();
            if (goal != null) allMessages.add(goal);
            if (systemPrompt != null) allMessages.add(systemPrompt);
        }
        Map<String, Object> stepMetadata = (step instanceof DefaultStep ds) ? ds.getMetadata() : null;
        return new ModelCall(new ModelCall.ModelCallRequest(allMessages, stepMetadata, getStepOutputInstructions()));
    }

    /**
     * Executes a model call with retry and memory snapshot/restore.
     */
    private ModelCallResponse executeModelCallWithRetry(ModelCall modelCall, AtomicInteger modelCallCount) {
        String provider = context.getModelConfig() != null ? context.getModelConfig().getProvider() : "spring";
        String conversationId = context.getConversationid();
        int maxRetries = 1; // Configurable if needed
        int attempt = 0;
        Exception lastException = null;
        while (attempt <= maxRetries) {
            context.getChatMemory().takeSnapshot(conversationId);
            try {
                return modelCallExecutorFactory.forProvider(provider, modelCall, context).execute();
            } catch (Exception e) {
                lastException = e;
                context.getChatMemory().restoreSnapshot(conversationId);
                attempt++;
                log.warn("Model call failed (attempt {}), retrying...", attempt, e);
            }
        }
        throw new RuntimeException("Model call failed after retries", lastException);
    }

    // --- Tool Call Logic ---

    /**
     * Handles tool calls from the model call response and updates the execution.
     */
    private void handleToolCalls(ModelCallResponse modelCallResponse, StepExecution execution) {
        List<ToolCall> toolCalls = modelCallResponse.getToolCalls();
        log.debug("Model call returned {} tool calls for step: {}", toolCalls.size(), step.getId());
        List<ToolCall> executedToolCalls = executeToolCallsWithRetry(toolCalls);
        execution.getToolCalls().addAll(executedToolCalls);
    }

    /**
     * Executes all tool calls for the step, with retry and memory snapshot/restore.
     */
    private List<ToolCall> executeToolCallsWithRetry(List<ToolCall> toolCalls) {
        List<ToolCall> executedToolCalls = new ArrayList<>();
        if (sequentialToolCalls) {
            for (ToolCall toolCall : toolCalls) {
                log.info("Executing tool call {} (sequential) for step: {}", toolCall.getName(), step.getId());
                executeToolCallWithRetry(toolCall);
                executedToolCalls.add(toolCall);
            }
        } else {
            List<CompletableFuture<Void>> futures = toolCalls.stream()
                    .map(tc -> CompletableFuture.runAsync(() -> {
                        log.info("Executing tool call {} (parallel) for step: {}", tc.getName(), step.getId());
                        executeToolCallWithRetry(tc);
                    }, executor))
                    .toList();
            futures.forEach(CompletableFuture::join);
            executedToolCalls.addAll(toolCalls);
        }
        return executedToolCalls;
    }

    /**
     * Executes a single tool call with retry and memory snapshot/restore.
     */
    private ToolCall.ToolCallResponse executeToolCallWithRetry(ToolCall toolCall) {
        int attempt = 0;
        boolean success = false;
        String provider = context.getModelConfig() != null ? context.getModelConfig().getProvider() : "spring";
        String conversationId = context.getConversationid();
        ToolCall.ToolCallResponse toolCallResponse = new ToolCall.ToolCallResponse(null, null);
        while (attempt <= maxToolRetries && !success) {
            context.getChatMemory().takeSnapshot(conversationId);
            try {
                toolCallResponse = toolCallExecutorFactory.forProvider(provider, toolCall, context).execute();
                if (toolCall.getStatus() == ToolCallStatus.COMPLETED) {
                    success = true;
                } else {
                    attempt++;
                    context.getChatMemory().restoreSnapshot(conversationId);
                }
            } catch (Exception e) {
                attempt++;
                context.getChatMemory().restoreSnapshot(conversationId);
                log.error("Tool call execution failed (attempt {}), retrying...", attempt, e);
            }
        }
        if (!success) {
            log.error("Tool call {} failed after {} attempts in step {}", toolCall.getName(), maxToolRetries + 1, step.getId());
        }
        return toolCallResponse;
    }

    // --- Step Completion Logic ---

    /**
     * Finalizes the step execution by advancing to the next step.
     */
    private void finalizeStepExecution(StepExecution execution) {
        stepManager.advanceToNextStep(context, toolCallExecutorFactory);
    }

    /**
     * Prepares a new StepExecution for the given step.
     */
    private StepExecution prepareStepExecution(Step step) {
        StepExecution execution = new StepExecution(step);
        step.setStepExecution(execution);
        execution.setModelCalls(new ArrayList<>());
        execution.setToolCalls(new ArrayList<>());
        return execution;
    }

    /**
     * Handles errors during step execution by logging and updating the execution status and error.
     */
    private void handleStepExecutionError(Exception e, StepExecution execution) {
        log.error("Step execution failed for step {}: {}", step.getId(), e.getMessage(), e);
        execution.fail(e.getMessage());
    }

    /**
     * Returns output instructions for the step (can be customized per step type).
     */
    private OutputInstructions getStepOutputInstructions() {
        return new StepCompletionOutputInstructions();
    }
}
 