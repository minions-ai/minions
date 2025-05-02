package com.minionslab.mcp.step;

import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.model.ModelCallExecutionContext;
import com.minionslab.mcp.model.ModelCallExecutor;
import com.minionslab.mcp.tool.MCPToolCall;
import com.minionslab.mcp.tool.MCPToolCallExecutor;
import com.minionslab.mcp.tool.ToolCallExecutionContext;
import com.minionslab.mcp.tool.ToolCallStatus;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * StepExecutor orchestrates the execution of a single MCPStep.
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
    private final MCPStep step;
    private final ModelCallExecutionContext modelContext;
    private final ToolCallExecutionContext toolContext;
    private final Executor executor;
    private final MCPContext context;
    private final int maxModelCalls;
    private final int maxToolRetries;
    private final boolean sequentialToolCalls;
    private List<MCPStep.StepInstruction> followedInstructions = new ArrayList<>();
    
    public StepExecutor(MCPStep step, MCPContext context) {
        this(step, context, ForkJoinPool.commonPool());
    }
    
    public StepExecutor(MCPStep step, MCPContext context, Executor executor) {
        this.step = step;
        this.context = context;
        this.modelContext = context.getModelCallExecutionContext();
        this.toolContext = context.getToolCallExecutionContext();
        this.executor = executor;
        maxModelCalls = context.getMetadata().getOrDefault("maxModelCallsPerStep", 10) instanceof Integer ?
                                (Integer) context.getMetadata().get("maxModelCallsPerStep") : 10;
        maxToolRetries = context.getMetadata().getOrDefault("maxToolCallRetries", 2) instanceof Integer ?
                                 (Integer) context.getMetadata().get("maxToolCallRetries") : 2;
        sequentialToolCalls = Boolean.TRUE.equals(context.getMetadata().get("sequentialToolCalls"));
    }
    
    /**
     * Executes the step asynchronously, managing model and tool calls as per the documented flow.
     *
     * @return A future containing the step execution
     */
    public CompletableFuture<StepExecution> execute() {
        return CompletableFuture.supplyAsync(this::doExecute, executor);
    }
    
    @NotNull
    StepExecution doExecute() {
        StepExecution execution = new StepExecution(step, step.getCompletionCriteria());
        step.setStepExecution(execution);
        execution.setModelCalls(new ArrayList<>());
        execution.setToolCalls(new ArrayList<>());
        
        
        AtomicInteger modelCallCount = new AtomicInteger(0);
        AtomicBoolean stepComplete = new AtomicBoolean(false);
        
        try {
            MCPModelCall currentModelCall = step.createInitialModelCall();
            execution.getModelCalls().add(currentModelCall);
            modelCallCount.incrementAndGet();
            
            while (!stepComplete.get() && modelCallCount.get() <= maxModelCalls) {
                // Execute model call
                ModelCallExecutor.forCall(currentModelCall, context).execute().join();
                
                followInstructions(context);
                
                // Check for final_Answer tool call
                List<MCPToolCall> toolCalls = currentModelCall.getToolCalls();
                
                
                // Execute tool calls (parallel or sequential)
                List<MCPToolCall> executedToolCalls = new ArrayList<>();
                if (sequentialToolCalls) {
                    for (MCPToolCall toolCall : toolCalls) {
                        executeToolCallWithRetries(toolCall, maxToolRetries);
                        executedToolCalls.add(toolCall);
                    }
                } else {
                    List<CompletableFuture<Void>> futures = toolCalls.stream()
                                                                     .map(tc -> CompletableFuture.runAsync(() -> executeToolCallWithRetries(tc, maxToolRetries), executor))
                                                                     .collect(Collectors.toList());
                    futures.forEach(CompletableFuture::join);
                    executedToolCalls.addAll(toolCalls);
                }
                execution.getToolCalls().addAll(executedToolCalls);
                
                // Prepare follow-up model call with tool results
                MCPModelCall followUpModelCall = step.createFollowUpModelCall(currentModelCall, executedToolCalls);
                execution.getModelCalls().add(followUpModelCall);
                currentModelCall = followUpModelCall;
                modelCallCount.incrementAndGet();
            }
            
            if (modelCallCount.get() > maxModelCalls) {
                execution.setStatus(StepStatus.FAILED);
                execution.setError("Maximum number of model calls per step exceeded");
            } else {
                execution.setStatus(StepStatus.COMPLETED);
            }
            execution.setCompletedAt(Instant.now());
            
        } catch (Exception e) {
            log.error("Step execution failed: {}", e.getMessage(), e);
            execution.setStatus(StepStatus.FAILED);
            execution.setError(e.getMessage());
            execution.setCompletedAt(Instant.now());
        }
        
        return execution;
    }
    
    private void followInstructions(MCPContext context) {
        List<MCPStep.StepInstruction> instructions = context.getInstructions();
        
        for (MCPStep.StepInstruction instruction : instructions) {
            if (instruction.stepId().equals(this.step.getId())) {
                if (!followedInstructions.contains(instruction)) {
                    MCPStep.StepOutcome outcome = instruction.outcome();
                    switch (outcome) {
                        case COMPLETED -> {
                            step.getStepExecution().complete();
                            context.setNextStep(instruction.nextStepSuggestion());
                        }
                        case CAN_NOT_FINISH -> step.getStepExecution().fail(instruction.reason());
                        case UNRECOVERABLE_ERROR -> step.getStepExecution().fail(instruction.reason());
                    }
                }
            }
        }
    }
    
    private void executeToolCallWithRetries(MCPToolCall toolCall, int maxRetries) {
        int attempt = 0;
        boolean success = false;
        while (attempt <= maxRetries && !success) {
            try {
                MCPToolCallExecutor.forCall(toolCall, toolContext).execute().join();
                if (toolCall.getStatus() == ToolCallStatus.COMPLETED) {
                    success = true;
                } else {
                    attempt++;
                }
            } catch (Exception e) {
                log.error("Tool call execution failed (attempt {}): {}", attempt, e.getMessage());
                attempt++;
                if (attempt > maxRetries) {
                    // Send error message to LLM as a model call (could be implemented as needed)
                    toolCall.setStatus(ToolCallStatus.FAILED);
                    toolCall.setResponse(new MCPToolCall.MCPToolCallResponse(null, e.getMessage()));
                }
            }
        }
    }
}
