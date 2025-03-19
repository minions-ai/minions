package com.minionslab.core.common.config;

import com.minionslab.core.domain.memory.CaffeineChatMemory;
import java.time.Duration;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for chat memory components.
 */
@Configuration
public class ChatMemoryConfig {

  @Value("${minions.chat.memory.type:CaffeineChatMemory}")
  private String chatMemoryType;

  /**
   * Creates a ChatMemory bean based on the configured minionType.
   */
  @Bean
  @Primary
  public ChatMemory chatMemory() {
    if ("CaffeineChatMemory".equals(chatMemoryType)) {
      return new CaffeineChatMemory();
    }
    throw new IllegalArgumentException("Unsupported chat memory minionType: " + chatMemoryType);
  }

  /**
   * Creates a CaffeineChatMemory bean with custom settings.
   */
  @Bean
  public ChatMemory customChatMemory() {
    return new CaffeineChatMemory(20, Duration.ofHours(48));
  }
} 