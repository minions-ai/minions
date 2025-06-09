package com.minionslab.core.common.message;

/**
 * Enum representing the possible roles of a message sender in the Model Context Protocol (MCP).
 */
public enum MessageRole {
    
    /**
     * Message from the user.
     */
    USER,
    
    /**
     * Message from the assistant or system.
     */
    ASSISTANT,
    
    /**
     * System message, typically used for instructions or context.
     */
    SYSTEM,
    TOOL,
    ERROR, GOAL
    
    
} 