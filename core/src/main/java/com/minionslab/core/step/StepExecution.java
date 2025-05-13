package com.minionslab.core.step;

import com.minionslab.core.model.ModelCall;
import com.minionslab.core.tool.ToolCall;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tracks the execution of a step, including all model and tool calls and their execution state.
 * This class is now refactored to remove CallGroup and track all calls chronologically.
 */
@Data
@Accessors(chain = true)
public class StepExecution {
    private final String id = UUID.randomUUID().toString();
    private final Step step;
    private final Instant startedAt;
    
    //todo figure out what to do with StepCompletionInstruction. Who decides if the step is completed.
    private StepCompletionOutputInstructions.StepCompletionInstruction completionResult;
    private Instant completedAt;
    private StepStatus status;
    private String error;
    
    // Chronological tracking of all model and tool calls
    private List<ModelCall> modelCalls = new ArrayList<>();
    private List<ToolCall> toolCalls = new ArrayList<>();
    
    public StepExecution(Step step) {
        this.step = step;
        this.startedAt = Instant.now();
        this.status = StepStatus.IN_PROGRESS;
    }
    
    /**
     * Checks if more model calls are needed (e.g., max not reached and not complete).
     *
     * @param maxModelCalls The maximum allowed model calls for this step
     * @return true if more calls are needed, false otherwise
     */
    public boolean requiresMoreCalls(int maxModelCalls) {
        return !isComplete() && modelCalls.size() < maxModelCalls;
    }
    
    /**
     * Checks if the step execution is complete based on the completion criteria or if any model call
     * contains a tool call named "final_Answer".
     *
     * @return true if the step is complete, false otherwise
     */
    public boolean isComplete() {
        // Complete if any tool call is named "final_Answer" or if status is COMPLETED/FAILED
        if (status == StepStatus.COMPLETED || status == StepStatus.FAILED)
            return true;
        return toolCalls.stream().anyMatch(tc -> "final_Answer".equals(tc.getName()));
    }
    
    /**
     * Marks this step execution as completed.
     */
    public void complete() {
        setCompleted();
    }
    
    /**
     * Internal: set status to COMPLETED and update completedAt.
     */
    public void setCompleted() {
        this.completedAt = Instant.now();
        this.status = StepStatus.COMPLETED;
    }
    
    /**
     * Marks this step execution as failed with the given error.
     *
     * @param error The error message
     */
    public void fail(String error) {
        failWithError(error);
    }
    
    /**
     * Internal: set status to FAILED, update completedAt, and set error.
     */
    public void failWithError(String error) {
        this.completedAt = Instant.now();
        this.status = StepStatus.FAILED;
        this.error = error;
    }
    
    /**
     * Marks this step execution as skipped.
     */
    public void skip() {
        this.completedAt = Instant.now();
        this.status = StepStatus.SKIPPED;
    }
    
    /**
     * Adds a model call to the execution.
     *
     * @param modelCall The model call to add
     */
    public void addModelCall(ModelCall modelCall) {
        this.modelCalls.add(modelCall);
    }
    
    /**
     * Adds a tool call to the execution.
     *
     * @param toolCall The tool call to add
     */
    public void addToolCall(ToolCall toolCall) {
        this.toolCalls.add(toolCall);
    }
    
    /**
     * Public getter for the step associated with this execution.
     */
    public Step getStep() {
        return this.step;
    }
} 