package com.minionslab.mcp.agent;

/**
 * Defines security policies for MCP agents.
 */
public enum SecurityPolicy {
    /**
     * No restrictions on tool usage or operations.
     */
    UNRESTRICTED,

    /**
     * Default security policy with standard restrictions.
     */
    DEFAULT,

    /**
     * Strict security policy with limited tool access.
     */
    STRICT,

    /**
     * Read-only operations allowed only.
     */
    READ_ONLY,

    /**
     * Custom security policy defined by configuration.
     */
    CUSTOM
} 