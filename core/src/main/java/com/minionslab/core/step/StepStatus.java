package com.minionslab.core.step;

/**
 * Enum representing the possible statuses of a step in the Model Context Protocol (MCP).
 */
public enum StepStatus {
    
    /**
     * The step is waiting to be executed.
     */
    PENDING,
    
    /**
     * The step is currently being executed.
     */
    IN_PROGRESS,
    
    /**
     * The step has been successfully completed.
     */
    COMPLETED,
    
    /**
     * The step failed to complete successfully.
     */
    FAILED,
    
    /**
     * The step was skipped or not executed.
     */
    SKIPPED
} 