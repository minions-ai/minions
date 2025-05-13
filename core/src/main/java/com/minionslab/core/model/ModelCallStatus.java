package com.minionslab.core.model;

/**
 * Represents the status of a model call in the Model Context Protocol.
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