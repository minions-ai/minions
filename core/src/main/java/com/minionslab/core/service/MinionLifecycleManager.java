package com.minionslab.core.service;

import com.minionslab.core.domain.Minion;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Centralized manager for minion lifecycle stages. Handles creation, initialization, monitoring, and shutdown of minions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinionLifecycleManager {

  private final PromptService promptService;
  private final ChatClient.Builder chatClientBuilder;
  private AIService aIService;

  public CompletableFuture<Void> initializeMinion(Minion minion) {
    return CompletableFuture.runAsync(() -> {
      try {
        // Load and set the prompt

        minion.setChatClient(aIService.getChatClient());

        // Initialize the minion
        minion.initialize();
      } catch (Exception e) {
        log.error("Failed to initialize minion: {}", minion.getMinionId(), e);
        minion.handleFailure(e);
      }
    });
  }

  public CompletableFuture<Void> shutdownMinion(Minion minion) {
    return CompletableFuture.runAsync(() -> {
      try {
        // Clean up resources
        minion.getMetrics().clear();
      } catch (Exception e) {
        log.error("Error during minion shutdown: {}", minion.getMinionId(), e);
        throw new IllegalStateException("Failed to shutdown minion", e);
      }
    });
  }

  public void handleFailure(Minion minion, Exception error) {
    log.error("Handling failure for minion: {}", minion.getMinionId(), error);
    // Add any failure handling logic here
  }


} 