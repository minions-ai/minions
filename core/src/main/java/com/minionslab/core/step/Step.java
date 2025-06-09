package com.minionslab.core.step;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.step.customizer.StepCustomizer;

/**
 * Step defines a unit of work in the Model Context Protocol (MCP) workflow.
 * Each step represents a logical action, decision, or transformation, and may
 * involve multiple model calls, tool invocations, or user interactions.
 * <p>
 * Steps are designed for extensibility: you can implement this interface to create
 * custom step types, override behavior, or add metadata. Steps can be dynamically
 * customized at runtime using {@link StepCustomizer}, enabling flexible workflows
 * and pluggable orchestration strategies.
 * <p>
 * Steps are typically composed into a graph or chain, supporting advanced execution
 * patterns such as branching, looping, and conditional transitions.
 * <p>
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Implement this interface to define custom step types for new actions, decisions, or workflow logic.</li>
 *   <li>Override methods to add metadata, custom goal logic, or new step outcomes.</li>
 *   <li>Use {@link StepCustomizer} to dynamically modify or decorate steps at runtime.</li>
 * </ul>
 * <b>Usage:</b> Steps are composed into graphs or chains for advanced workflow orchestration. Extend for custom branching, looping, or conditional logic.
 */
public interface Step {
    /**
     * Applies a customizer to this step, allowing dynamic modification or decoration.
     *
     * @param customizer the customizer to apply
     */
    void customize(StepCustomizer customizer);

    /**
     * Gets the unique identifier for this step.
     *
     * @return The step ID
     */
    String getId();

    /**
     * Gets the goal or description of what this step does.
     *
     * @return The step goal as a Message
     */
    Message getGoal();

    /**
     * Gets the system prompt or context for this step, if any.
     *
     * @return The system prompt message, or null if not applicable
     */
    Message getSystemPrompt();

    /**
     * Gets the type identifier for this step (used for serialization, dispatch, etc).
     *
     * @return The step type string
     */
    String getType();

    /**
     * Enum representing possible outcomes of a step's execution.
     * Used for controlling workflow transitions and error handling.
     */
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

        /**
         * Gets the string value of this outcome.
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * Gets a human-readable description of this outcome.
         * @return the description
         */
        public String getDescription() {
            return description;
        }
    }
}