package com.minionslab.core.model;

/**
 * Exception thrown when a model call execution fails.
 */
public class ModelCallExecutionException extends RuntimeException {
    
    public ModelCallExecutionException(String message) {
        super(message);
    }
    
    public ModelCallExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
} 