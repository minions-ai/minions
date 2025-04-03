package com.minionslab.core.common.exception;

/**
 * Exception thrown when validation fails.
 * This is a wrapper exception that provides more context about validation failures.
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
} 