package com.minionslab.core.service;

import com.minionslab.core.domain.ChatMemoryStrategyType;
import com.minionslab.core.domain.memory.CaffeineChatMemory;
import com.minionslab.core.service.impl.llm.MinionHybridMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Factory class for creating ChatMemory instances based on the specified strategy type.
 * This factory follows the Factory pattern to encapsulate the creation logic for different
 * ChatMemory implementations.
 */
@Slf4j
@Component
public class ChatMemoryFactory {

    @Value("${minions.chat.memory.max-messages:10}")
    private int defaultMaxMessages;

    @Value("${minions.chat.memory.expiration-hours:24}")
    private int defaultExpirationHours;

    @Value("${minions.chat.memory.type:CaffeineChatMemory}")
    private String defaultMemoryType;

    @Autowired(required = false)
    private VectorStoreProvider vectorStoreProvider;

    /**
     * Creates a ChatMemory instance based on the specified strategy type.
     *
     * @param strategyType The type of memory strategy to use
     * @return A ChatMemory implementation appropriate for the strategy type
     */
    public ChatMemory createChatMemory(ChatMemoryStrategyType strategyType) {
        log.debug("Creating ChatMemory for strategy type: {}", strategyType);
        
        return switch (strategyType) {
            case MESSAGE -> createMessageMemory();
            case PROMPT -> createPromptMemory();
            case VECTOR -> createVectorMemory();
        };
    }

    /**
     * Creates a ChatMemory instance based on the default configuration.
     *
     * @return A ChatMemory implementation based on the default configuration
     */
    public ChatMemory createDefaultChatMemory() {
        log.debug("Creating default ChatMemory of type: {}", defaultMemoryType);
        
        if ("CaffeineChatMemory".equals(defaultMemoryType)) {
            return new CaffeineChatMemory(defaultMaxMessages, Duration.ofHours(defaultExpirationHours));
        } else if ("InMemoryChatMemory".equals(defaultMemoryType)) {
            return new InMemoryChatMemory();
        } else {
            log.warn("Unknown memory type: {}, falling back to InMemoryChatMemory", defaultMemoryType);
            return new InMemoryChatMemory();
        }
    }

    /**
     * Creates a message-based memory implementation.
     * This is typically used for storing and retrieving conversation messages.
     *
     * @return A ChatMemory implementation for message-based memory
     */
    private ChatMemory createMessageMemory() {
        log.debug("Creating message-based ChatMemory");
        return new CaffeineChatMemory(defaultMaxMessages, Duration.ofHours(defaultExpirationHours));
    }

    /**
     * Creates a prompt-based memory implementation.
     * This is typically used for storing and retrieving prompts.
     *
     * @return A ChatMemory implementation for prompt-based memory
     */
    private ChatMemory createPromptMemory() {
        log.debug("Creating prompt-based ChatMemory");
        return new CaffeineChatMemory(defaultMaxMessages, Duration.ofHours(defaultExpirationHours));
    }

    /**
     * Creates a vector-based memory implementation.
     * This is typically used for semantic search and retrieval.
     *
     * @return A ChatMemory implementation for vector-based memory
     */
    private ChatMemory createVectorMemory() {
        log.debug("Creating vector-based ChatMemory");
        if (vectorStoreProvider != null) {

            // If vector store provider is available, create a vector-based memory
            // This is a placeholder for future implementation
            log.info("Vector store provider available, but vector memory not fully implemented yet");
            return new MinionHybridMemory();
        } else {
            log.warn("Vector store provider not available, falling back to CaffeineChatMemory");
            return new CaffeineChatMemory(defaultMaxMessages, Duration.ofHours(defaultExpirationHours));
        }
    }
} 