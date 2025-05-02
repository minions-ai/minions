package com.minionslab.mcp.model;

import com.minionslab.mcp.step.MCPStep;
import com.minionslab.mcp.util.MCPMessageSpringConverter;
import lombok.experimental.Accessors;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

/**
 * Context for executing model calls, containing all necessary components
 * for message handling, model interaction, and tool call extraction.
 */
@Accessors(chain = true)
public record ModelCallExecutionContext(
        ChatModel chatModel,
        ChatMemoryRepository chatMemory,
        String conversationId,
        ToolCallingChatOptions chatOptions,
        MCPMessageSpringConverter messageConverter,
        BeanOutputConverter converter

) {
    /**
     * Creates a new builder for ModelCallExecutionContext.
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    
    /**
     * Builder for ModelCallExecutionContext.
     */
    public static class Builder {
        private ChatModel chatModel;
        private ChatMemoryRepository chatMemory;
        private String conversationId;
        private ToolCallingChatOptions chatOptions;
        private MCPMessageSpringConverter messageConverter;
        private BeanOutputConverter converter = new BeanOutputConverter(MCPStep.StepInstruction.class);
        
        
        public Builder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }
        
        public Builder chatMemory(ChatMemoryRepository chatMemory) {
            this.chatMemory = chatMemory;
            return this;
        }
        
        public Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
            return this;
        }
        
        public Builder chatOptions(ToolCallingChatOptions chatOptions) {
            this.chatOptions = chatOptions;
            return this;
        }
        
        public Builder messageConverter(MCPMessageSpringConverter messageConverter) {
            this.messageConverter = messageConverter;
            return this;
        }
        
        
        public ModelCallExecutionContext build() {
            if (messageConverter == null) {
                messageConverter = new MCPMessageSpringConverter();
            }
            
            return new ModelCallExecutionContext(
                    chatModel, chatMemory, conversationId,
                    chatOptions, messageConverter, converter
            );
        }
    }
} 