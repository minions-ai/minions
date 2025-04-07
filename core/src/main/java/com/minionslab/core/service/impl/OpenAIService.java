package com.minionslab.core.service.impl;

import com.minionslab.core.service.AIService;
import com.minionslab.core.service.ChatMemoryFactory;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.DefaultChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenAIService implements AIService {

  private ChatClient.Builder chatClientBuilder;
  private ChatMemoryFactory chatMemoryFactory;

  @Autowired
  public OpenAIService(ChatClient.Builder chatClientBuilder, ChatMemoryFactory chatMemoryFactory) {
    this.chatClientBuilder = chatClientBuilder;
    this.chatMemoryFactory = chatMemoryFactory;
  }

  @Override public ChatClient getChatClient() {
    ChatClient chatClient = chatClientBuilder
        .defaultOptions(new DefaultChatOptions())
        .defaultAdvisors(
            new PromptChatMemoryAdvisor(chatMemoryFactory.createDefaultChatMemory())
            , new SimpleLoggerAdvisor(0))
        .defaultToolContext(new ConcurrentHashMap<>())
        .build();
    return chatClient;
  }
}
