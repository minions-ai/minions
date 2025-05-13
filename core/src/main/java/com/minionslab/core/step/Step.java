package com.minionslab.core.step;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.minionslab.core.message.Message;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.tool.ToolCall;

import java.util.Optional;
import java.util.Set;

/**
 * Interface defining a step in the Model Context Protocol.
 * A step represents a unit of work that may involve multiple model and tool calls.
 */
public interface Step {
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
    Set<String> getAvailableTools();
    
    
    /**
     * Gets the description of what this step does.
     *
     * @return The step description
     */
    Message getGoal();
    

    
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
    
    /**
     * Returns an optional decision tool call for this step. If present, this tool will be used
     * to select the next step when multiple possible next steps exist.
     *
     * @return Optional ToolCall for decision/orchestration
     */
    Optional<ToolCall> getDecisionToolCall();
    
    Message getSystemPrompt();
    
    
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    enum StepOutcome {
        CAN_NOT_FINISH("CAN_NOT_FINISH", "Cannot finish"),
        UNRECOVERABLE_ERROR("UNRECOVERABLE_ERROR", "Unrecoverable error"),
        COMPLETED("COMPLETED", "Completed"),
        AWAITING_TOOL_RESULTS("AWAITING_TOOL_RESULTS", "Awaiting tool results"),
        CONTINUE("CONTINUE", "Continue step"),
        SKIPPED("SKIPPED", "Step skipped");
        
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
} 