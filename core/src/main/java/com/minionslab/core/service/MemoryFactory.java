package com.minionslab.core.service;

import com.minionslab.core.domain.ChatMemoryStrategyType;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for creating memory instances based on strategy type.
 * This class delegates to the ChatMemoryFactory for creating ChatMemory instances.
 */
@Component
public class MemoryFactory {

  @Autowired
  private ChatMemoryFactory chatMemoryFactory;

  /**
   * Creates a ChatMemory instance based on the specified strategy type.
   *
   * @param strategyType The type of memory strategy to use
   * @return A ChatMemory implementation appropriate for the strategy type
   */
  public ChatMemory getMemory(ChatMemoryStrategyType strategyType) {
    return chatMemoryFactory.createChatMemory(strategyType);
  }
}
