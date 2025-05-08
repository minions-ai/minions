package com.minionslab.mcp.message;

import com.minionslab.mcp.tool.MCPToolCall;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents a message in the Model Context Protocol.
 * Messages are the fundamental unit of communication in MCP.
 */
public interface MCPMessage {
    
    
    String getId();
    
    /**
     * Gets the role of the message sender.
     *
     * @return The message role (system, user, assistant, tool)
     */
    MessageRole getRole();
    
    /**
     * Gets the content of the message.
     *
     * @return The message content
     */
    String getContent();
    
    /**
     * Gets the timestamp when the message was created.
     *
     * @return The message timestamp
     */
    Instant getTimestamp();
    
    /**
     * Gets the metadata associated with this message.
     *
     * @return Map of metadata key-value pairs
     */
    Map<String, Object> getMetadata();
    
    /**
     * Gets the calls of the tool if this is a tool message.
     *
     * @return The tool calls or null if not a tool message
     */
//    List<MCPToolCall> getToolCalls();
    
    /**
     * Gets the model ID if this is a model message.
     *
     * @return The model ID or null if not a model message
     */
    String getModelId();
    
    /**
     * Gets the token count for this message.
     *
     * @return The number of tokens in this message
     */
    int getTokenCount();
    
    
} 