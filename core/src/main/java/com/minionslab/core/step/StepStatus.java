package com.minionslab.core.step;

/**
 * Enum representing the possible statuses of a step in the Model Context Protocol (MCP).
 *
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Add new statuses as needed for custom step lifecycle management.</li>
 *   <li>Use StepStatus to track and manage step state transitions.</li>
 * </ul>
 * <b>Usage:</b> Use StepStatus to represent and manage the lifecycle of steps in the workflow.
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