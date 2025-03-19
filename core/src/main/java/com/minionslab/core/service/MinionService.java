package com.minionslab.core.service;

import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.common.exception.MinionException.ContextCreationException;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionRegistry;
import com.minionslab.core.domain.MinionFactory;
import com.minionslab.core.domain.enums.MinionType;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j @Service public class MinionService {

  private final MinionFactory minionFactory;
  private final MinionRegistry minionRegistry;
  private final ContextService contextService;

  public MinionService(MinionFactory minionFactory, MinionRegistry minionRegistry, ContextService contextService) {
    this.minionFactory = minionFactory;
    this.minionRegistry = minionRegistry;
    this.contextService = contextService;
  }

  /**
   * Create a new minion based on the creation request
   *
   * @param minionType The type of the minion
   * @param metadata   The metadata for the minion
   * @return The created minion
   */
  public Minion createMinion(MinionType minionType, Map<String, String> metadata, MinionPrompt prompt) throws MinionException {

    try {
      validateCreateMinionInputs(minionType);

      contextService.createContext();
      Minion minion = minionFactory.createMinion(minionType, metadata, prompt);

      // Set metadata if provided
      if (metadata != null && !metadata.isEmpty()) {
        minion.setMetadata(metadata);
      }

      minionRegistry.registerAgent(minion);

      log.info("Created new minion with ID: {}", minion.getMinionId());
      return minion;

    } catch (ContextCreationException e) {
      log.error("Failed to create minion: {}", minionType, e);
      throw new MinionException.CreationException("Failed to create minion: " + e.getMessage(), e);
    }
  }

  private void validateCreateMinionInputs(MinionType minionType) {

    if (minionType == null) {
      throw new IllegalArgumentException("Minion type cannot be null");
    }


  }


  public Minion getMinionById(String minionId) {
    return minionRegistry.getMinionById(minionId);
  }
}
