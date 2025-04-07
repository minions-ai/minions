package com.minionslab.core.service.impl;

import com.minionslab.core.common.exception.MinionException.MinionCreationException;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.event.MinionEventPublisher;
import com.minionslab.core.service.LLMService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Factory class for creating Minion instances. This class should only be accessed through MinionCreationService. Direct instantiation or
 * injection is not allowed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Validated
public class MinionFactory {

  private final MinionEventPublisher eventPublisher;
  private final LLMService llmService;

  /**
   * Creates a new Minion instance with the specified type, metadata, and prompt.
   *
   * @param minionType The type of minion to create
   * @param metadata   Additional metadata for the minion
   * @param prompt     The prompt to use for the minion
   * @return A new Minion instance
   * @throws MinionCreationException if minion creation fails
   */
  public Minion createMinion(@Valid @NotNull MinionType minionType, Map<String, Object> metadata, @Valid @NotNull MinionPrompt prompt) {
    try {
      log.debug("Creating minion of type: {} with prompt: {}", minionType, prompt != null ? prompt.getId() : "null");

      // Create minion instance using builder pattern
      Minion minion = Minion.builder()
          .minionType(minionType)
          .minionPrompt(prompt)
          .llmService(llmService)
          .eventPublisher(eventPublisher)
          .build();

      if (metadata != null) {
        minion.getMetadata().putAll(metadata);
      }
      return minion;
    } catch (Exception e) {
      log.error("Failed to create minion of type: {}", minionType, e);
      throw new MinionCreationException("Failed to create minion: " + e.getMessage(), e);
    }
  }
} 