package com.minionslab.core.model;

/**
 * Represents the status of a model call in the Model Context Protocol.
 *
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Add new statuses as needed for custom model call lifecycle management.</li>
 *   <li>Use ModelCallStatus to track and manage model call state transitions.</li>
 * </ul>
 * <b>Usage:</b> Use ModelCallStatus to represent and manage the lifecycle of model calls in the workflow.
 */
public enum ModelCallStatus {
    /**
     * The model call is waiting to be executed.
     */
    PENDING,

    /**
     * The model call is currently being executed.
     */
    EXECUTING,

    /**
     * The model call has completed successfully.
     */
    COMPLETED,

    /**
     * The model call failed to complete.
     */
    FAILED
} 