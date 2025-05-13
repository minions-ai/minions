package com.minionslab.core.agent;

/**
 * Represents the possible states of a goal in the MCP system.
 */
public enum GoalStatus {
    /**
     * Goal has been created but work hasn't started.
     */
    PENDING,

    /**
     * Work on the goal is in progress.
     */
    IN_PROGRESS,

    /**
     * Goal has been completed successfully.
     */
    COMPLETED,

    /**
     * Goal was not completed successfully.
     */
    FAILED,

    /**
     * Goal was cancelled before completion.
     */
    CANCELLED,

    /**
     * Goal is blocked by dependencies or constraints.
     */
    BLOCKED
} 