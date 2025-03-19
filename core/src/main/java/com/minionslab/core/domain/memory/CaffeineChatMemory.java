package com.minionslab.core.domain.memory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Component;

/**
 * Implementation of ChatMemory using Caffeine cache for efficient message storage and retrieval. This implementation provides thread-safe
 * message storage with automatic expiration.
 */
@Slf4j
@Component("CaffeineChatMemory")
public class CaffeineChatMemory implements ChatMemory {

  private static final int DEFAULT_MAX_MESSAGES = 10;
  private static final Duration DEFAULT_EXPIRATION = Duration.ofHours(24);
  private final Cache<String, List<Message>> messageCache;
  private final Map<String, Integer> maxMessageCounts;

  /**
   * Creates a new CaffeineChatMemory with default settings.
   */
  public CaffeineChatMemory() {
    this(DEFAULT_MAX_MESSAGES, DEFAULT_EXPIRATION);
  }

  /**
   * Creates a new CaffeineChatMemory with custom settings.
   *
   * @param maxMessages The maximum number of messages to store per conversation
   * @param expiration  The duration after which messages expire
   */
  public CaffeineChatMemory(int maxMessages, Duration expiration) {
    this.messageCache = Caffeine.newBuilder()
        .expireAfterWrite(expiration)
        .build();
    this.maxMessageCounts = new ConcurrentHashMap<>();
  }

  @Override
  public void add(String conversationId, List<Message> messages) {
    log.debug("Adding {} messages to conversation: {}", messages.size(), conversationId);
    List<Message> existingMessages = messageCache.get(conversationId, k -> new ArrayList<>());
    existingMessages.addAll(messages);

    // Trim to max size if needed
    int maxMessages = maxMessageCounts.getOrDefault(conversationId, DEFAULT_MAX_MESSAGES);
    if (existingMessages.size() > maxMessages) {
      existingMessages.subList(0, existingMessages.size() - maxMessages).clear();
    }
  }

  @Override
  public List<Message> get(String conversationId, int maxMessages) {
    log.debug("Retrieving up to {} messages for conversation: {}", maxMessages, conversationId);
    List<Message> messages = messageCache.getIfPresent(conversationId);
    if (messages == null) {
      return new ArrayList<>();
    }

    if (messages.size() <= maxMessages) {
      return new ArrayList<>(messages);
    }

    return new ArrayList<>(messages.subList(messages.size() - maxMessages, messages.size()));
  }

  @Override
  public void clear(String conversationId) {
    log.debug("Clearing messages for conversation: {}", conversationId);
    messageCache.invalidate(conversationId);
  }


  private String formatMessageHistory(List<Message> messages) {
    StringBuilder history = new StringBuilder();
    for (Message message : messages) {
      String role = message.getMessageType() == MessageType.USER ? "Human" : "Assistant";
      history.append(role).append(": ").append(message.getContent()).append("\n");
    }
    return history.toString();
  }
} 