package com.minionslab.core.model;

import com.minionslab.core.common.util.MessageConverter;
import lombok.experimental.Accessors;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

/**
 * Context for executing model calls, containing all necessary components
 * for message handling, model interaction, and tool call extraction.
 *
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Extend ModelCallExecutionContext to add custom fields, adapters, or orchestration logic for model execution.</li>
 *   <li>Use the builder to construct custom execution contexts for advanced scenarios.</li>
 * </ul>
 * <b>Usage:</b> ModelCallExecutionContext encapsulates all dependencies for executing a model call, including chat model, memory, and options.
 */

@Accessors(chain = true)
public record ModelCallExecutionContext(
        ChatModel chatModel,
        ChatMemoryRepository chatMemory,
        String conversationId,
        ToolCallingChatOptions chatOptions

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
        private MessageConverter messageConverter;
        
        
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
        
        public Builder messageConverter(MessageConverter messageConverter) {
            this.messageConverter = messageConverter;
            return this;
        }
        
        
        public ModelCallExecutionContext build() {
            
            return new ModelCallExecutionContext(
                    chatModel, chatMemory, conversationId,
                    chatOptions
            );
        }
    }
} 