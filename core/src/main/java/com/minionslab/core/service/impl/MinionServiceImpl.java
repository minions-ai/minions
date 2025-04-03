package com.minionslab.core.service.impl;

import com.minionslab.core.api.dto.CreateMinionRequest;
import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.common.exception.MinionException.ContextCreationException;
import com.minionslab.core.common.exception.MinionException.MinionNotFoundException;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionFactory;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.MinionRecipe;
import com.minionslab.core.domain.MinionRecipeRegistry;
import com.minionslab.core.domain.MinionRegistry;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.repository.MinionRepository;
import com.minionslab.core.service.ContextService;
import com.minionslab.core.service.MinionService;
import com.minionslab.core.service.PromptService;
import com.minionslab.core.service.impl.llm.LLMServiceFactory;
import com.minionslab.core.service.impl.llm.model.LLMRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class MinionServiceImpl implements MinionService {

  private final MinionFactory minionFactory;
  private final MinionRegistry minionRegistry;
  private final ContextService contextService;
  private final MinionRecipeRegistry recipeRegistry;
  private final MinionRepository minionRepository;
  private final PromptService promptService;
  private final LLMServiceFactory llmServiceFactory;

  /**
   * Create a new minion based on the creation request
   *
   * @param minionType The type of the minion
   * @param metadata   The metadatas for the minion
   * @param prompt     The prompt to use for the minion
   * @return The created minion
   */
  public Minion createMinion(MinionType minionType, Map<String, Object> metadata, MinionPrompt prompt) throws MinionException {
    try {
      validateCreateMinionInputs(minionType, prompt);

      // Get the recipe for this minion type
      MinionRecipe recipe = recipeRegistry.getRecipe(minionType);

      // Validate the prompt against the recipe
      recipe.validatePrompt(prompt);

      // Create context
      contextService.createContext();

      // Merge metadata with defaults
      Map<String, Object> finalMetadata = new HashMap<>();
      recipe.getDefaultMetadata().forEach((key, value) ->
          finalMetadata.put(key, value.toString()));
      if (metadata != null) {
        finalMetadata.putAll(metadata);
      }

      // Create the minion
      Minion minion = minionFactory.createMinion(minionType, finalMetadata, prompt);

      // Register the minion
      minionRegistry.registerMinion(minion);

      log.info("Created new minion with ID: {}", minion.getMinionId());
      return minion;

    } catch (ContextCreationException e) {
      log.error("Failed to create minion: {}", minionType, e);
      throw new MinionException.CreationException("Failed to create minion: " + e.getMessage(), e);
    }
  }

  private void validateCreateMinionInputs(MinionType minionType, MinionPrompt prompt) {
    if (minionType == null) {
      throw new IllegalArgumentException("Minion type cannot be null");
    }
    if (prompt == null) {
      throw new IllegalArgumentException("Prompt cannot be null");
    }
  }

  public Minion getMinionById(String minionId) {
    return minionRegistry.getMinionById(minionId);
  }

  @Override
  @Transactional
  public Minion createMinion(CreateMinionRequest minionRequest) {

    // Validate that the prompt exists
    Instant effectiveDate = minionRequest.getEffectiveDate() != null ? minionRequest.getEffectiveDate() : Instant.now();
    MinionPrompt prompt = promptService.getActivePromptAt(minionRequest.getPromptEntityId(), effectiveDate)
        .orElseThrow(() -> new IllegalArgumentException("Prompt not found: " + minionRequest.getPromptEntityId()));

    Minion minion = minionFactory.createMinion(minionRequest.getMinionType(), minionRequest.getMetadata(), prompt);



/*
    todo: Figure out whether a minion should be saved
// Save the minion
    Minion savedMinion = minionRepository.save(minion);
    log.debug("Created minion: {}", savedMinion.getId());*/

    return minion;
  }

  @Override
  @Transactional(readOnly = true)
  public String processRequest(String minionId, String request) {
    log.debug("Processing request for minion: {}", minionId);

    // Get the minion
    Minion minion = getMinion(minionId);

/*    // Get the current prompt version
    MinionPrompt prompt = promptService.getPrompt(minion.getPromptId())
        .orElseThrow(() -> new IllegalArgumentException("Prompt not found: " + minion.getPromptId()));*/

    // Process the request using the LLM service
    LLMRequest llmRequest = new LLMRequest().setPrompt(minion.getMinionPrompt()).setUserRequest(request);
    return llmServiceFactory.getLLMService()
        .processRequest(llmRequest)
        .getResponseText();
  }

  @Override
  @Transactional(readOnly = true)
  public Minion getMinion(String minionId) {
    log.debug("Retrieving minion: {}", minionId);

    return minionRepository.findById(minionId)
        .orElseThrow(() -> new MinionNotFoundException("Minion not found: " + minionId));
  }
}
