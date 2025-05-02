package com.minionslab.mcp.agent;

/**
 * Represents the possible states of a goal in the MCP system.
 */
public enum GoalStatus {
    /**
     * MCPGoal has been created but work hasn't started.
     */
    PENDING,

    /**
     * Work on the goal is in progress.
     */
    IN_PROGRESS,

    /**
     * MCPGoal has been completed successfully.
     */
    COMPLETED,

    /**
     * MCPGoal was not completed successfully.
     */
    FAILED,

    /**
     * MCPGoal was cancelled before completion.
     */
    CANCELLED,

    /**
     * MCPGoal is blocked by dependencies or constraints.
     */
    BLOCKED
} 