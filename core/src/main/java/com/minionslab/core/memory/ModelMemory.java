package com.minionslab.core.memory;

import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.model.Prompt;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;

/**
 * Wrapper for ChatMemory that provides Message-centric operations and policy hooks.
 */
public abstract class ModelMemory {
    protected ModelMemory() {
    }
    
    /**
     * Saves all MCPMessages to the chat memory for the given conversation.
     *
     * @param conversationId The conversation ID
     * @param messages       The messages to save
     */
    public abstract void saveAll(String conversationId, List<Message> messages);
    
    /**
     * Retrieves all MCPMessages for the given conversation.
     *
     * @param conversationId The conversation ID
     * @return List of MCPMessages
     */
    public abstract List<Message> findByConversationId(String conversationId);
    
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
    
    
    public abstract void savePrompt(String conversationId, Prompt prompt);
    
    
    //todo this is not a good api. We should not have any dependency on Spring AI here.
    public abstract void saveChatResponse(String conversationId, ChatResponse response);
    
    /**
     * Retrieves all MCPMessages for the given conversation and scope.
     *
     * @param conversationId The conversation ID
     * @param scope The message scope to filter by
     * @return List of MCPMessages with the given scope
     */
    public abstract List<Message> findByScope(String conversationId, MessageScope scope);
    public abstract List<Message> findByScopeAndRole(String conversationId, MessageScope scope, MessageRole role);
    
    /**
     * Retrieves the policy-filtered messages to be included in the prompt for the next model/agent call.
     *
     * @param conversationId The conversation ID
     * @return List of MCPMessages for prompt assembly
     */
    public abstract List<Message> getPromptMessages(String conversationId);

    // Snapshot/restore support
    public abstract void takeSnapshot(String conversationId);
    public abstract void restoreSnapshot(String conversationId);
}

