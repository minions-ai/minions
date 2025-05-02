package com.minionslab.mcp.service;


import com.minionslab.mcp.config.ModelConfig;
import com.minionslab.mcp.context.MCPContext;

/**
 * Main interface for the Model Context Protocol (MCP) service.
 * This service handles the core protocol operations including context management,
 * model interactions, and tool executions.
 */
public interface MCPService {
    
    /**
     * Processes a request through the MCP protocol using the context.
     *
     * @param context The complete context including model recipe, tools, memory, and conversation state
     * @return Updated context after processing
     */

    
    /**
     * Validates a context according to protocol specifications.
     *
     * @param context The context to validate
     * @throws IllegalArgumentException if the context is invalid
     */
    void validateContext(MCPContext context);
    
    /**
     * Sets the default model configuration for subsequent operations.
     * This is used as a fallback when an agent doesn't provide its own configuration.
     *
     * @param modelConfig The model configuration to use
     */
    void setModelConfig(ModelConfig modelConfig);
    
    /**
     * Gets the default model configuration.
     *
     * @return The current default model configuration
     */
    ModelConfig getModelConfig();
} 