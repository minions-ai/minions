package com.minionslab.core.service.impl;

import com.minionslab.core.service.AIService;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.DefaultChatOptions;
import org.springframework.stereotype.Service;

@Service
public class OpenAIService implements AIService {

  private ChatClient.Builder chatClientBuilder;

  @Override public ChatClient getChatClient() {
    ChatClient chatClient = chatClientBuilder
        .defaultOptions(new DefaultChatOptions())
        .defaultAdvisors(
            new PromptChatMemoryAdvisor(new InMemoryChatMemory())
            , new SimpleLoggerAdvisor(0))
        .defaultToolContext(new ConcurrentHashMap<>())
        .build();
    return chatClient;
  }
}
