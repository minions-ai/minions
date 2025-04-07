package com.minionslab.core.common.config;

import com.minionslab.core.service.ChatMemoryFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for chat memory components.
 */
@Configuration
public class ChatMemoryConfig {

  /**
   * Creates a ChatMemory bean using the ChatMemoryFactory.
   */
  @Bean
  @Primary
  public ChatMemory chatMemory(ChatMemoryFactory chatMemoryFactory) {
    return chatMemoryFactory.createDefaultChatMemory();
  }
} 