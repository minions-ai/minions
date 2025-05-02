package com.minionslab.mcp.step;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.minionslab.mcp.model.MCPModelCall;

import java.util.Map;
import java.util.Set;

/**
 * Interface defining a step in the Model Context Protocol.
 * A step represents a unit of work that may involve multiple model and tool calls.
 */
public interface MCPStep {
    /**
     * Gets the unique identifier for this step.
     *
     * @return The step ID
     */
    String getId();
    
    /**
     * Gets the set of tools required by this step.
     *
     * @return Set of required tool names
     */
    Set<String> getRequiredTools();
    
    
    /**
     * Gets the description of what this step does.
     *
     * @return The step description
     */
    String getDescription();
    
    /**
     * Creates the initial model call for this step.
     * This is called at the start of step execution.
     *
     * @return The initial model call
     */
    MCPModelCall createInitialModelCall();
    
    /**
     * Creates a follow-up model call after tool calls have been executed.
     * This is called after tool calls complete to process their results.
     *
     * @param previousModelCall The previous model call
     * @param toolCalls The list of tool calls executed after the previous model call
     * @return The follow-up model call
     */
    MCPModelCall createFollowUpModelCall(MCPModelCall previousModelCall, java.util.List<com.minionslab.mcp.tool.MCPToolCall> toolCalls);
    
    /**
     * Gets the completion criteria for this step.
     * This determines when the step is considered complete.
     *
     * @return The completion criteria
     */
    StepCompletionCriteria getCompletionCriteria();
    
    /**
     * Gets the execution result for this step.
     *
     * @return The StepExecution for this step
     */
    StepExecution getStepExecution();
    
    /**
     * Sets the execution result for this step.
     *
     * @param execution The StepExecution to set
     */
    void setStepExecution(StepExecution execution);
    
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    enum StepOutcome {
        CAN_NOT_FINISH("can_not_finish", "Cannot finish"),
        UNRECOVERABLE_ERROR("unrecoverable_error", "Unrecoverable error"),
        COMPLETED("completed", "Completed");
        
        @JsonValue
        private final String value;
        
        private final String description;
        
        StepOutcome(String value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public String getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    
    record StepInstruction(String stepId,
                           String result,
                           StepOutcome outcome,
                           String reason,
                           String timestamp,
                           Map<String, Object> metadata,
                           Double confidence,
                           String nextStepSuggestion) {
        
        
    }
}