package com.minionslab.mcp.memory.springai;

import com.minionslab.mcp.memory.MCPChatMemory;
import com.minionslab.mcp.message.MCPMessage;
import com.minionslab.mcp.util.MCPtoSpringConverter;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;
import java.util.stream.Collectors;

public class SpringAIChatMemory extends MCPChatMemory {
    private final ChatMemoryRepository delegate;
    
    public SpringAIChatMemory(ChatMemoryRepository delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void savePrompt(String conversationId, com.minionslab.mcp.model.MCPPrompt prompt) {
        // Convert MCPPrompt to Spring Prompt
        List<Message> springMessages = prompt.getMessages().stream()
                .map(MCPtoSpringConverter::toSpringMessage)
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
        List<com.minionslab.mcp.message.MCPMessage> mcpMessages = springPrompt.getInstructions().stream()
                .map(MCPtoSpringConverter::toMCPMessage)
                .collect(Collectors.toList());
        saveAll(conversationId, mcpMessages);
    }
    
    @Override
    public void saveAll(String conversationId, List<MCPMessage> messages) {
        List<Message> springMessages = messages.stream()
                                               .map(MCPtoSpringConverter::toSpringMessage)
                                               .collect(Collectors.toList());
        delegate.saveAll(conversationId, springMessages);
    }
    
    @Override
    public void saveChatResponse(String conversationId, ChatResponse response) {
        List<MCPMessage> mcpMessages = response.getResults().stream()
                                               .map(generation -> MCPtoSpringConverter.toMCPMessage((Message) generation.getOutput()))
                                               .collect(Collectors.toList());
        saveAll(conversationId, mcpMessages);
    }
    
    @Override
    public List<MCPMessage> findByConversationId(String conversationId) {
        List<Message> springMessages = delegate.findByConversationId(conversationId);
        return springMessages.stream()
                             .map(MCPtoSpringConverter::toMCPMessage)
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
    
    public List<Message> findSpringMessagesByConversationId(String conversationId) {
        return delegate.findByConversationId(conversationId);
    }
} 