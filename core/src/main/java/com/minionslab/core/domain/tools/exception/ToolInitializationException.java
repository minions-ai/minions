package com.minionslab.core.domain.tools.exception;

/**
 * Exception thrown when there are issues during tool initialization
 */
public class ToolInitializationException extends ToolException {
    
    public ToolInitializationException(String message) {
        super(message);
    }

    public ToolInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
} 