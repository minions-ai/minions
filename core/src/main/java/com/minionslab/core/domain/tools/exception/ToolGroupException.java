package com.minionslab.core.domain.tools.exception;

/**
 * Exception thrown when there are issues with tool groups
 */
public class ToolGroupException extends ToolException {
    
    public ToolGroupException(String groupId) {
        super(String.format("Invalid tool group: %s", groupId));
    }

    public ToolGroupException(String groupId, String message) {
        super(String.format("Tool group error for %s: %s", groupId, message));
    }

    public ToolGroupException(String message, Throwable cause) {
        super(message, cause);
    }
} 