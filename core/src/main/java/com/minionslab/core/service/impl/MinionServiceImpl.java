package com.minionslab.core.service.impl;

import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.common.exception.MinionException.ContextCreationException;
import com.minionslab.core.common.exception.MinionException.MinionCreationException;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.MinionRecipe;
import com.minionslab.core.domain.MinionRecipeRegistry;
import com.minionslab.core.domain.MinionRegistry;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.repository.MinionRepository;
import com.minionslab.core.repository.PromptRepository;
import com.minionslab.core.service.MinionCreationService;
import com.minionslab.core.service.MinionService;
import com.minionslab.core.service.PromptService;
import com.minionslab.core.service.impl.llm.LLMServiceFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

  private final MinionRecipeRegistry recipeRegistry;
  private final MinionRepository minionRepository;
  private final PromptRepository promptRepository;
  private final PromptService promptService;
  private final LLMServiceFactory llmServiceFactory;

  private final MinionCreationService minionCreationService;

  /**
   * Create a new minion based on the creation request
   *
   * @param minionType The type of the minion
   * @param metadata   The metadatas for the minion
   * @param prompt     The prompt to use for the minion
   * @return The created minion
   */
  public Minion createMinion(@NotNull MinionType minionType, Map<String, Object> metadata, @Valid MinionPrompt prompt)
      throws MinionException {
    try {
      validateCreateMinionInputs(minionType, prompt);

      // Get the recipe for this minion type
      MinionRecipe recipe = recipeRegistry.getRecipe(minionType);

      // Validate the prompt against the recipe
      recipe.validatePrompt(prompt);

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
      throw new MinionCreationException("Failed to create minion: " + e.getMessage(), e);
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
    return minionRegistry.getMinion(minionId);
  }

  @Override
  @Transactional
  public Minion createMinion(
      @NotNull MinionType minionType,
      Map<String, Object> metadata,
      @NotBlank String promptEntityId,
      Instant effectiveDate,
      Instant expiryDate) {
        
    try {
      // Get the prompt
      Optional<MinionPrompt> promptOpt = promptService.getPrompt(promptEntityId);
      MinionPrompt prompt = promptOpt.orElseThrow(() -> 
          new MinionCreationException("Prompt not found: " + promptEntityId));
      
      // Create the minion
      Minion minion = minionCreationService.createMinion(minionType, metadata, prompt);
      
      // Save to repository
      minionRepository.save(minion);
      
      log.info("Created new minion with ID: {}", minion.getMinionId());
      return minion;
      
    } catch (ContextCreationException e) {
      log.error("Failed to create minion: {}", minionType, e);
      throw new MinionCreationException("Failed to create minion: " + e.getMessage(), e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public String processRequest(@NotBlank String minionId, @NotBlank String request, Map<String, Object> context) {
    Minion minion = getMinion(minionId);
    if (minion == null) {
      throw new MinionException.MinionNotFoundException(minionId);
    }
    
    try {
      return minion.processPrompt(request, context);
    } catch (Exception e) {
      log.error("Failed to process request for minion: {}", minionId, e);
      throw new MinionException.ProcessingException("Failed to process request", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Minion getMinion(@NotBlank String minionId) {
    return minionRegistry.getMinion(minionId);
  }
}
