package com.minionslab.mcp.memory;

import com.minionslab.mcp.message.MCPMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

/**
 * Wrapper for ChatMemory that provides MCPMessage-centric operations and policy hooks.
 */
public abstract class MCPChatMemory {
    protected MCPChatMemory() {
    }
    
    /**
     * Saves all MCPMessages to the chat memory for the given conversation.
     *
     * @param conversationId The conversation ID
     * @param messages       The messages to save
     */
    public abstract void saveAll(String conversationId, List<MCPMessage> messages);
    
    /**
     * Retrieves all MCPMessages for the given conversation.
     *
     * @param conversationId The conversation ID
     * @return List of MCPMessages
     */
    public abstract List<MCPMessage> findByConversationId(String conversationId);
    
    /**
     * Clears all messages for the given conversation.
     *
     * @param conversationId The conversation ID
     */
    public abstract void clear(String conversationId);
    
    /**
     * Returns a summary of the conversation (if supported by the underlying memory).
     *
     * @param conversationId The conversation ID
     * @return Summary string or null if not supported
     */
    public abstract String summarize(String conversationId);
    
    
    public abstract void savePrompt(String conversationId, com.minionslab.mcp.model.MCPPrompt prompt);
    
    public abstract void saveChatResponse(String conversationId, ChatResponse response);
}

