package com.minionslab.core.tool;

/**
 * Represents the status of a tool call in the Model Context Protocol.
 *
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Add new statuses as needed for custom tool call lifecycle management.</li>
 *   <li>Use ToolCallStatus to track and manage tool call state transitions.</li>
 * </ul>
 * <b>Usage:</b> Use ToolCallStatus to represent and manage the lifecycle of tool calls in the workflow.
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