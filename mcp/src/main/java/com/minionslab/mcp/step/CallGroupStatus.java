package com.minionslab.mcp.step;

/**
 * Status of a call group execution
 */
public enum CallGroupStatus {
    /**
     * The call group is waiting to be executed
     */
    PENDING,

    /**
     * The initial model call is being executed
     */
    EXECUTING_INITIAL_MODEL_CALL,

    /**
     * Tool calls are being executed
     */
    EXECUTING_TOOL_CALLS,

    /**
     * The follow-up model call is being executed
     */
    EXECUTING_FOLLOW_UP_MODEL_CALL,

    /**
     * All calls in the group have completed successfully
     */
    COMPLETED,

    /**
     * The call group failed to complete
     */
    FAILED
} 