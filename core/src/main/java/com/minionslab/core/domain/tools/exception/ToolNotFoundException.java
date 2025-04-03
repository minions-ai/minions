package com.minionslab.core.domain.tools.exception;

/**
 * Exception thrown when a requested tool is not found in the registry
 */
public class ToolNotFoundException extends ToolException {
    
    public ToolNotFoundException(String toolName) {
        super(String.format("Tool not found: %s", toolName));
    }

    public ToolNotFoundException(String toolName, String version) {
        super(String.format("Tool not found: %s with version: %s", toolName, version));
    }

    public ToolNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 