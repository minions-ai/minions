package com.minionslab.mcp.agent;

/**
 * Represents the possible states of an MCP agent.
 */
public enum AgentStatus {
    /**
     * Agent has been created but not yet initialized.
     */
    CREATED,

    /**
     * Agent has been initialized and is ready to process requests.
     */
    INITIALIZED,

    /**
     * Agent is currently processing a request.
     */
    PROCESSING,

    /**
     * Agent is waiting for external input or resource.
     */
    WAITING,

    /**
     * Agent has encountered an error.
     */
    ERROR,

    /**
     * Agent has been shut down.
     */
    SHUTDOWN
} 