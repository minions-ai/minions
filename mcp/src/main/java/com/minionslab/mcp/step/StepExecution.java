package com.minionslab.mcp.step;

import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.tool.MCPToolCall;
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
    private final StepCompletionCriteria completionCriteria;
    private Instant completedAt;
    private StepStatus status;
    private String error;

    // Chronological tracking of all model and tool calls
    private List<MCPModelCall> modelCalls = new ArrayList<>();
    private List<MCPToolCall> toolCalls = new ArrayList<>();

    public StepExecution(Step step, StepCompletionCriteria criteria) {
        this.step = step;
        this.startedAt = Instant.now();
        this.status = StepStatus.IN_PROGRESS;
        this.completionCriteria = criteria;
    }

    /**
     * Checks if the step execution is complete based on the completion criteria or if any model call
     * contains a tool call named "final_Answer".
     *
     * @return true if the step is complete, false otherwise
     */
    public boolean isComplete() {
        // Complete if any tool call is named "final_Answer" or if status is COMPLETED/FAILED
        if (status == StepStatus.COMPLETED || status == StepStatus.FAILED) return true;
        return toolCalls.stream().anyMatch(tc -> "final_Answer".equals(tc.getName()));
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
     * Marks this step execution as completed.
     */
    public void complete() {
        setCompleted();
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
     * Marks this step execution as skipped.
     */
    public void skip() {
        this.completedAt = Instant.now();
        this.status = StepStatus.SKIPPED;
    }

    /**
     * Internal: set status to COMPLETED and update completedAt.
     */
    public void setCompleted() {
        this.completedAt = Instant.now();
        this.status = StepStatus.COMPLETED;
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
     * Adds a model call to the execution.
     *
     * @param modelCall The model call to add
     */
    public void addModelCall(MCPModelCall modelCall) {
        this.modelCalls.add(modelCall);
    }

    /**
     * Adds a tool call to the execution.
     *
     * @param toolCall The tool call to add
     */
    public void addToolCall(MCPToolCall toolCall) {
        this.toolCalls.add(toolCall);
    }
} 