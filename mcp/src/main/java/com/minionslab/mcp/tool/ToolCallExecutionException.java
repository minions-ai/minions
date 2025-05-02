package com.minionslab.mcp.tool;

/**
 * Exception thrown when a tool call execution fails.
 */
public class ToolCallExecutionException extends RuntimeException {
    
    public ToolCallExecutionException(String message) {
        super(message);
    }
    
    public ToolCallExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
} 