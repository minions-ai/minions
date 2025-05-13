package com.minionslab.core.agent;

/**
 * Exception thrown when agent execution encounters an error.
 */
public class AgentExecutionException extends RuntimeException {
    
    public AgentExecutionException(String message) {
        super(message);
    }
    
    public AgentExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
} 