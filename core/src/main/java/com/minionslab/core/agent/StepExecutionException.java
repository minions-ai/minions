package com.minionslab.core.agent;

/**
 * Exception thrown when a step execution fails in the Model Context Protocol.
 */
public class StepExecutionException extends RuntimeException {
    
    public StepExecutionException(String message) {
        super(message);
    }
    
    public StepExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
} 