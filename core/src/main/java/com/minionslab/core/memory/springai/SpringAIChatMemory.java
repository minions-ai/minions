package com.minionslab.core.memory.springai;

import com.minionslab.core.memory.ModelMemory;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.model.Prompt;
import com.minionslab.core.util.MessageConverter;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;
import java.util.stream.Collectors;

public class SpringAIChatMemory extends ModelMemory {
    private final ChatMemoryRepository delegate;
    
    public SpringAIChatMemory(ChatMemoryRepository delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void savePrompt(String conversationId, Prompt prompt) {
        // Convert Prompt to Spring Prompt
        List<org.springframework.ai.chat.messages.Message> springMessages = prompt.getMessages().stream()
                                                                                  .map(MessageConverter::toSpringMessage)
                                                                                  .collect(Collectors.toList());
        // Extract chatOptions if present
        org.springframework.ai.model.tool.ToolCallingChatOptions chatOptions = null;
        if (prompt.getOptions() != null && prompt.getOptions().get("chatOptions") instanceof org.springframework.ai.model.tool.ToolCallingChatOptions) {
            chatOptions = (org.springframework.ai.model.tool.ToolCallingChatOptions) prompt.getOptions().get("chatOptions");
        }
        org.springframework.ai.chat.prompt.Prompt springPrompt = chatOptions != null ?
                new org.springframework.ai.chat.prompt.Prompt(springMessages, chatOptions) :
                new org.springframework.ai.chat.prompt.Prompt(springMessages);
        // Save as MCPMessages
        List<Message> messages = springPrompt.getInstructions().stream()
                                             .map(MessageConverter::toMCPMessage)
                                             .collect(Collectors.toList());
        saveAll(conversationId, messages);
    }
    
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        List<org.springframework.ai.chat.messages.Message> springMessages = messages.stream()
                                                                                    .map(MessageConverter::toSpringMessage)
                                                                                    .collect(Collectors.toList());
        delegate.saveAll(conversationId, springMessages);
    }
    
    @Override
    public void saveChatResponse(String conversationId, ChatResponse response) {
        List<Message> messages = response.getResults().stream()
                                         .map(generation -> MessageConverter.toMCPMessage((org.springframework.ai.chat.messages.Message) generation.getOutput()))
                                         .collect(Collectors.toList());
        saveAll(conversationId, messages);
    }
    
    @Override
    public List<Message> findByScope(String conversationId, MessageScope scope) {
        return List.of();
    }
    
    @Override
    public List<Message> findByScopeAndRole(String conversationId, MessageScope scope, MessageRole role) {
        return List.of();
    }
    
    @Override
    public List<Message> getPromptMessages(String conversationId) {
        return List.of();
    }
    
    @Override
    public List<Message> findByConversationId(String conversationId) {
        List<org.springframework.ai.chat.messages.Message> springMessages = delegate.findByConversationId(conversationId);
        return springMessages.stream()
                             .map(MessageConverter::toMCPMessage)
                             .collect(Collectors.toList());
    }
    
    @Override
    public void clear(String conversationId) {
        delegate.deleteByConversationId(conversationId);
    }
    
    @Override
    public String summarize(String conversationId) {
        // Placeholder: implement summarization policy if needed
        return null;
    }
    
    public List<org.springframework.ai.chat.messages.Message> findSpringMessagesByConversationId(String conversationId) {
        return delegate.findByConversationId(conversationId);
    }
} 