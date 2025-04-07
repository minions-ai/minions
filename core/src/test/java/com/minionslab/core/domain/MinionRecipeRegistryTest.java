package com.minionslab.core.domain;

import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class MinionRecipeRegistryTest {

  private MinionRecipeRegistry registry;
  private MinionRecipe userDefinedRecipe;
  private MinionRecipe communicationRecipe;

  @BeforeEach
  void setUp() {
    registry = new MinionRecipeRegistry();

    // Create USER_DEFINED_AGENT recipe
    Set<PromptType> userDefinedComponents = new HashSet<>();
    userDefinedComponents.add(PromptType.SYSTEM);
    userDefinedComponents.add(PromptType.USER_TEMPLATE);

    Set<String> userDefinedMetadata = new HashSet<>();
    userDefinedMetadata.add("model");
    userDefinedMetadata.add("temperature");

    Map<String, Object> userDefinedDefaults = new HashMap<>();
    userDefinedDefaults.put("model", "gpt-4");
    userDefinedDefaults.put("temperature", 0.7);

    userDefinedRecipe = MinionRecipe.builder()
        .type(MinionType.USER_DEFINED_AGENT)
        .requiredComponents(userDefinedComponents)
        .requiredMetadata(userDefinedMetadata)
        .defaultMetadata(userDefinedDefaults)
        .description("User defined agent recipe")
        .requiresTenant(false)
        .build();

    // Create COMMUNICATION_AGENT recipe
    Set<PromptType> communicationComponents = new HashSet<>();
    communicationComponents.add(PromptType.SYSTEM);
    communicationComponents.add(PromptType.PERSONA);
    communicationComponents.add(PromptType.USER_TEMPLATE);

    Set<String> communicationMetadata = new HashSet<>();
    communicationMetadata.add("model");
    communicationMetadata.add("temperature");
    communicationMetadata.add("max_tokens");

    Map<String, Object> communicationDefaults = new HashMap<>();
    communicationDefaults.put("model", "gpt-4");
    communicationDefaults.put("temperature", 0.7);
    communicationDefaults.put("max_tokens", 2000);

    communicationRecipe = MinionRecipe.builder()
        .type(MinionType.COMMUNICATION_AGENT)
        .requiredComponents(communicationComponents)
        .requiredMetadata(communicationMetadata)
        .defaultMetadata(communicationDefaults)
        .description("Communication agent recipe")
        .requiresTenant(true)
        .build();

    // Register recipes
    registry.registerRecipe(userDefinedRecipe);
    registry.registerRecipe(communicationRecipe);
  }

  @Test
  @DisplayName("Should register and retrieve recipe successfully")
  void registerAndGetRecipe_ShouldWorkCorrectly() {
    MinionRecipe retrievedRecipe = registry.getRecipe(MinionType.USER_DEFINED_AGENT);
    assertNotNull(retrievedRecipe);
    assertEquals(userDefinedRecipe, retrievedRecipe);
  }

  @Test
  @DisplayName("Should throw exception when getting non-existent recipe")
  void getRecipe_WithNonExistentType_ShouldThrowException() {
    MinionException.RecipeNotFoundException exception = assertThrows(
        MinionException.RecipeNotFoundException.class,
        () -> registry.getRecipe(MinionType.WORKFLOW_MANAGER)
    );

    assertTrue(exception.getMessage().contains("No recipe found for type: WORKFLOW_MANAGER"));
  }

  @Test
  @DisplayName("Should validate prompt using recipe")
  void validatePrompt_ShouldUseCorrectRecipe() {
    MinionPrompt prompt = MinionPrompt.builder()
        .components(new HashMap<>())
        .metadata(new HashMap<>())
        .build();

    prompt.getComponents().put(PromptType.SYSTEM, PromptComponent.builder().type(PromptType.SYSTEM).text("system").build());
    prompt.getComponents().put(PromptType.USER_TEMPLATE, PromptComponent.builder().type(PromptType.USER_TEMPLATE).text("user").build());
    prompt.getMetadata().put("model", "gpt-4");
    prompt.getMetadata().put("temperature", "0.7");

    assertDoesNotThrow(() -> registry.validatePrompt(MinionType.USER_DEFINED_AGENT, prompt));
  }

  @Test
  @DisplayName("Should throw exception when validating prompt with non-existent recipe")
  void validatePrompt_WithNonExistentRecipe_ShouldThrowException() {
    MinionPrompt prompt = MinionPrompt.builder()
        .components(new HashMap<>())
        .metadata(new HashMap<>())
        .build();


    MinionException.RecipeNotFoundException exception = assertThrows(
        MinionException.RecipeNotFoundException.class,
        () -> registry.validatePrompt(MinionType.WORKFLOW_MANAGER, prompt)
    );

  }

  @Test
  @DisplayName("Should return correct default metadata for recipe")
  void getDefaultMetadata_ShouldReturnCorrectMap() {
    Map<String, Object> metadata = registry.getDefaultMetadata(MinionType.COMMUNICATION_AGENT);
    assertNotNull(metadata);
    assertEquals(communicationRecipe.getDefaultMetadata(), metadata);
  }

  @Test
  @DisplayName("Should throw exception when getting metadata for non-existent recipe")
  void getDefaultMetadata_WithNonExistentRecipe_ShouldThrowException() {
    MinionException.RecipeNotFoundException exception = assertThrows(
        MinionException.RecipeNotFoundException.class,
        () -> registry.getDefaultMetadata(MinionType.WORKFLOW_MANAGER)
    );

    assertTrue(exception.getMessage().contains("No recipe found for type: WORKFLOW_MANAGER"));
  }

  @Test
  @DisplayName("Should check tenant requirement correctly")
  void requiresTenant_ShouldReturnCorrectValue() {
    assertTrue(registry.requiresTenant(MinionType.COMMUNICATION_AGENT));
    assertFalse(registry.requiresTenant(MinionType.USER_DEFINED_AGENT));
  }

  @Test
  @DisplayName("Should throw exception when checking tenant requirement for non-existent recipe")
  void requiresTenant_WithNonExistentRecipe_ShouldThrowException() {
    MinionException.RecipeNotFoundException exception = assertThrows(
        MinionException.RecipeNotFoundException.class,
        () -> registry.requiresTenant(MinionType.WORKFLOW_MANAGER)
    );

    assertTrue(exception.getMessage().contains("No recipe found for type: WORKFLOW_MANAGER"));
  }
} 