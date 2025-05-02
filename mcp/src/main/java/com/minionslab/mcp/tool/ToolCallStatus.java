package com.minionslab.mcp.tool;

/**
 * Represents the status of a tool call in the Model Context Protocol.
 */
public enum ToolCallStatus {
    /**
     * The tool call is waiting to be executed.
     */
    PENDING,

    /**
     * The tool call is currently being executed.
     */
    EXECUTING,

    /**
     * The tool call has completed successfully.
     */
    COMPLETED,

    /**
     * The tool call failed to complete.
     */
    FAILED
} 