package com.minionslab.mcp.step;

/**
 * Represents the type of call in the Model Context Protocol.
 * A call can be either a model call or a tool call.
 */
public enum CallType {
    /**
     * Represents a call to a language model.
     */
    MODEL,

    /**
     * Represents a call to a tool.
     */
    TOOL
} 