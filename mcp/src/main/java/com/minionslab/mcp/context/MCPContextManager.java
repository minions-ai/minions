package com.minionslab.mcp.context;


import com.minionslab.mcp.message.MCPMessage;
import com.minionslab.mcp.step.MCPStep;

import java.util.List;

/**
 * Manages MCP context operations including message history, token tracking,
 * and context window management.
 */
public interface MCPContextManager {
    
    /**
     * Adds a message to the context.
     *
     * @param context The current context
     * @param message The message to add
     * @return Updated context with the new message
     */
    MCPContext addMessage(MCPContext context, MCPMessage message);
    
    /**
     * Adds a step to the context.
     *
     * @param context The current context
     * @param step The step to add
     * @return Updated context with the new step
     */
    MCPContext addStep(MCPContext context, MCPStep step);
    
    /**
     * Gets the current token count in the context.
     *
     * @param context The context to check
     * @return The total number of tokens
     */
    int getCurrentTokenCount(MCPContext context);
    
    /**
     * Optimizes the context by removing less relevant information
     * when approaching token limits.
     *
     * @param context The context to optimize
     * @param maxTokens The maximum allowed tokens
     * @return Optimized context
     */
    MCPContext optimizeContext(MCPContext context, int maxTokens);
    
    /**
     * Gets relevant messages for the current request.
     *
     * @param context The current context
     * @return List of relevant messages
     */
    List<MCPMessage> getRelevantMessages(MCPContext context);
    
    /**
     * Validates that the context is within token limits.
     *
     * @param context The context to validate
     * @param maxTokens The maximum allowed tokens
     * @throws IllegalStateException if token limit is exceeded
     */
    void validateTokenLimit(MCPContext context, int maxTokens);
    
    /**
     * Creates a summary of the context for long conversations.
     *
     * @param context The context to summarize
     * @return Summarized context
     */
    MCPContext summarizeContext(MCPContext context);
} 