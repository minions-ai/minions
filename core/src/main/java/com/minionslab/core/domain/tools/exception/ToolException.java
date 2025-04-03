package com.minionslab.core.domain.tools.exception;

/**
 * Base exception class for all tool-related exceptions
 */
public class ToolException extends RuntimeException {
    
    public ToolException(String message) {
        super(message);
    }

    public ToolException(String message, Throwable cause) {
        super(message, cause);
    }
} 