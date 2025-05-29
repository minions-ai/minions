package com.minionslab.core.agent;

/**
 * Represents the possible states of an MCP agent.
 *
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Add new statuses as needed for custom agent lifecycle management.</li>
 *   <li>Use AgentStatus to track and manage agent state transitions.</li>
 * </ul>
 * <b>Usage:</b> Use AgentStatus to represent and manage the lifecycle of agents in the framework.
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