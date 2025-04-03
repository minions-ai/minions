package com.minionslab.core.domain;

import com.minionslab.core.common.exception.MinionCreationException;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.tools.ToolRegistry;
import com.minionslab.core.service.MinionLifecycleManager;
import com.minionslab.core.service.PromptResolver;
import com.minionslab.core.service.resolver.PromptResolverChainFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Factory class for creating Minion instances based on their minionType and configuration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinionFactory {

  private final ChatMemory chatMemory;
  private final MinionLifecycleManager lifecycleManager;
  private final MinionRegistry minionRegistry;
  private final ToolRegistry toolRegistry;
  private final PromptResolverChainFactory resolverChainFactory;
  private final PromptResolver promptResolver;
  private final MinionRecipeRegistry recipeRegistry;

  /**
   * Creates a basic minion with default configuration.
   *
   * @param minionType The type of minion to create
   * @return A new Minion instance
   * @throws MinionCreationException if the minion cannot be created
   */
  public Minion createMinion(MinionType minionType) {
    if (minionType == null) {
      throw new MinionCreationException("MinionType cannot be null");
    }
    
    log.debug("Creating minion of type: {}", minionType);
    
    try {
      MinionRecipe recipe = recipeRegistry.getRecipe(minionType);
      if (recipe == null) {
        throw new MinionCreationException("No recipe found for minion type: " + minionType);
      }
      
      Minion minion = Minion.builder()
          .minionType(minionType)
          .metadata(convertMetadata(recipe.getDefaultMetadata()))
          .chatMemory(chatMemory)
          .build();
      
      // Register tools from recipe
      if (recipe.getRequiredToolboxes() != null && !recipe.getRequiredToolboxes().isEmpty()) {
        registerToolsForMinion(minion, recipe.getRequiredToolboxes());
      }
      
      // Initialize the minion
      lifecycleManager.initializeMinion(minion);
      
      log.info("Created minion of type: {}", minionType);
      return minion;
    } catch (Exception e) {
      log.error("Failed to create minion of type: {}", minionType, e);
      throw new MinionCreationException("Failed to create minion: " + e.getMessage(), e);
    }
  }

  /**
   * Creates a minion with custom metadata.
   *
   * @param minionType The type of minion to create
   * @param metadata Custom metadata for the minion
   * @return A new Minion instance
   * @throws MinionCreationException if the minion cannot be created
   */
  public Minion createMinion(MinionType minionType, Map<String, String> metadata) {
    if (minionType == null) {
      throw new MinionCreationException("MinionType cannot be null");
    }
    
    log.debug("Creating minion of type: {} with custom metadata", minionType);
    
    try {
      MinionRecipe recipe = recipeRegistry.getRecipe(minionType);
      if (recipe == null) {
        throw new MinionCreationException("No recipe found for minion type: " + minionType);
      }
      
      Map<String, Object> finalMetadata = convertMetadata(recipe.getDefaultMetadata());
      if (metadata != null) {
        finalMetadata.putAll(metadata);
      }

      Minion minion = Minion.builder()
          .minionType(minionType)
          .metadata(finalMetadata)
          .chatMemory(chatMemory)
          .build();
      
      // Register tools from recipe
      if (recipe.getRequiredToolboxes() != null && !recipe.getRequiredToolboxes().isEmpty()) {
        registerToolsForMinion(minion, recipe.getRequiredToolboxes());
      }
      
      // Initialize the minion
      lifecycleManager.initializeMinion(minion);
      
      log.info("Created minion of type: {} with custom metadata", minionType);
      return minion;
    } catch (Exception e) {
      log.error("Failed to create minion of type: {} with custom metadata", minionType, e);
      throw new MinionCreationException("Failed to create minion: " + e.getMessage(), e);
    }
  }

  /**
   * Creates a minion with custom metadata and a specific prompt.
   *
   * @param minionType The type of minion to create
   * @param metadata Custom metadata for the minion
   * @param prompt The prompt to use for the minion
   * @return A new Minion instance
   * @throws MinionCreationException if the minion cannot be created
   */
  public Minion createMinion(MinionType minionType, Map<String, Object> metadata, MinionPrompt prompt) {
    if (minionType == null) {
      throw new MinionCreationException("MinionType cannot be null");
    }
    
    if (prompt == null) {
      throw new MinionCreationException("Prompt cannot be null");
    }
    
    log.debug("Creating minion of type: {} with prompt: {}", minionType, prompt.getId());
    
    try {
      MinionRecipe recipe = recipeRegistry.getRecipe(minionType);
      if (recipe == null) {
        throw new MinionCreationException("No recipe found for minion type: " + minionType);
      }
      
      // Validate prompt against recipe
      recipe.validatePrompt(prompt);
      
      Map<String, Object> finalMetadata = convertMetadata(recipe.getDefaultMetadata());
      if (metadata != null) {
        finalMetadata.putAll(metadata);
      }
      
      // Combine toolboxes from recipe and prompt
      Set<String> toolboxNames = new HashSet<>();
      if (recipe.getRequiredToolboxes() != null) {
        toolboxNames.addAll(recipe.getRequiredToolboxes());
      }
      if (prompt.getToolboxes() != null) {
        toolboxNames.addAll(prompt.getToolboxes());
      }
      
      Minion minion = Minion.builder()
          .minionType(minionType)
          .minionPrompt(prompt)
          .chatMemory(chatMemory)
          .metadata(finalMetadata)
          .build();
      
      // Register tools for the minion
      if (!toolboxNames.isEmpty()) {
        registerToolsForMinion(minion, toolboxNames);
      }
      
      // Initialize the minion
      lifecycleManager.initializeMinion(minion);
      
      log.info("Created minion of type: {} with prompt: {}", minionType, prompt.getId());
      return minion;
    } catch (Exception e) {
      log.error("Failed to create minion of type: {} with prompt: {}", minionType, 
          prompt != null ? prompt.getId() : "null", e);
      throw new MinionCreationException("Failed to create minion: " + e.getMessage(), e);
    }
  }

  /**
   * Registers tools for a minion based on its toolbox names.
   *
   * @param minion The minion to register tools for
   * @param toolboxNames The names of the toolboxes to register
   */
  private void registerToolsForMinion(Minion minion, Set<String> toolboxNames) {
    if (minion == null) {
      throw new MinionCreationException("Minion cannot be null");
    }
    
    if (toolboxNames == null || toolboxNames.isEmpty()) {
      log.debug("No toolboxes to register for minion: {}", minion.getMinionId());
      return;
    }
    
    log.debug("Registering {} toolboxes for minion: {}", toolboxNames.size(), minion.getMinionId());
    
    try {
      // Register each toolbox
      for (String toolboxName : toolboxNames) {
        if (!StringUtils.hasText(toolboxName)) {
          log.warn("Empty toolbox name found, skipping");
          continue;
        }
        
        Object toolbox = toolRegistry.getToolbox(toolboxName);
        if (toolbox != null) {
          minion.getToolboxNames().add(toolboxName);
          log.debug("Registered toolbox: {} for minion: {}", toolboxName, minion.getMinionId());
        } else {
          log.warn("Toolbox not found: {} for minion: {}", toolboxName, minion.getMinionId());
        }
      }
    } catch (Exception e) {
      log.error("Failed to register tools for minion: {}", minion.getMinionId(), e);
      throw new MinionCreationException("Failed to register tools for minion: " + e.getMessage(), e);
    }
  }

  /**
   * Converts a Map<String, Object> to Map<String, Object> by ensuring all values are properly converted.
   * This method handles null values and ensures proper type conversion.
   */
  private Map<String, Object> convertMetadata(Map<String, Object> metadata) {
    if (metadata == null) {
      return new HashMap<>();
    }
    
    return metadata.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            entry -> entry.getValue() != null ? entry.getValue() : null
        ));
  }
}