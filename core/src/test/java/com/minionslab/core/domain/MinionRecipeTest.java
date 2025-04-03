package com.minionslab.core.domain;

import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class MinionRecipeTest {

  private MinionRecipe recipe;
  private Set<PromptType> requiredComponents;
  private Set<String> requiredMetadata;
  private Map<String, Object> defaultMetadata;

  @BeforeEach
  void setUp() {
    requiredComponents = new HashSet<>();
    requiredComponents.add(PromptType.SYSTEM);
    requiredComponents.add(PromptType.USER_TEMPLATE);

    requiredMetadata = new HashSet<>();
    requiredMetadata.add("model");
    requiredMetadata.add("temperature");

    defaultMetadata = new HashMap<>();
    defaultMetadata.put("model", "gpt-4");
    defaultMetadata.put("temperature", 0.7);

    recipe = MinionRecipe.builder()
        .type(MinionType.USER_DEFINED_AGENT)
        .requiredComponents(requiredComponents)
        .requiredMetadata(requiredMetadata)
        .defaultMetadata(defaultMetadata)
        .description("Test recipe")
        .requiresTenant(false)
        .build();
  }

  @Test
  @DisplayName("Should validate prompt with all required components")
  void validatePrompt_WithAllRequiredComponents_ShouldNotThrowException() {
    Map<PromptType, PromptComponent> components = new HashMap<>();
    Map<String, Object> metadata = new HashMap<>();

    components.put(PromptType.SYSTEM, PromptComponent.builder().type(PromptType.SYSTEM).text("system").build());
    components.put(PromptType.USER_TEMPLATE, PromptComponent.builder().type(PromptType.USER_TEMPLATE).text("user").build());
    metadata.put("model", "gpt-4");
    metadata.put("temperature", 0.7);

    MinionPrompt prompt = MinionPrompt.builder()
        .components(components)
        .metadata(metadata)
        .build();

    assertDoesNotThrow(() -> recipe.validatePrompt(prompt));
  }

  @Test
  @DisplayName("Should throw exception when prompt is null")
  void validatePrompt_WithNullPrompt_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> recipe.validatePrompt(null));
  }

  @Test
  @DisplayName("Should throw exception when required component is missing")
  void validatePrompt_WithMissingRequiredComponent_ShouldThrowException() {
    Map<PromptType, PromptComponent> components = new HashMap<>();
    Map<String, Object> metadata = new HashMap<>();

    components.put(PromptType.SYSTEM, PromptComponent.builder().type(PromptType.SYSTEM).text("system").build());
    metadata.put("model", "gpt-4");
    metadata.put("temperature", 0.7);

    MinionPrompt prompt = MinionPrompt.builder()
        .components(components)
        .metadata(metadata)
        .build();

    MinionException.InvalidPromptException exception = assertThrows(
        MinionException.InvalidPromptException.class,
        () -> recipe.validatePrompt(prompt)
    );

    assertTrue(exception.getMessage().contains("Missing required component type: USER_TEMPLATE"));
  }

  @Test
  @DisplayName("Should throw exception when required metadata is missing")
  void validatePrompt_WithMissingRequiredMetadata_ShouldThrowException() {
    Map<PromptType, PromptComponent> components = new HashMap<>();
    Map<String, Object> metadata = new HashMap<>();

    components.put(PromptType.SYSTEM, PromptComponent.builder().type(PromptType.SYSTEM).text("system").build());
    components.put(PromptType.USER_TEMPLATE, PromptComponent.builder().type(PromptType.USER_TEMPLATE).text("user").build());
    metadata.put("model", "gpt-4");

    MinionPrompt prompt = MinionPrompt.builder()
        .components(components)
        .metadata(metadata)
        .build();

    MinionException.InvalidPromptException exception = assertThrows(
        MinionException.InvalidPromptException.class,
        () -> recipe.validatePrompt(prompt)
    );

    assertTrue(exception.getMessage().contains("Missing required metadata key: temperature"));
  }

  @Test
  @DisplayName("Should return correct default metadata")
  void getDefaultMetadata_ShouldReturnCorrectMap() {
    assertEquals(defaultMetadata, recipe.getDefaultMetadata());
  }

  @Test
  @DisplayName("Should return correct required components")
  void getRequiredComponents_ShouldReturnCorrectSet() {
    assertEquals(requiredComponents, recipe.getRequiredComponents());
  }

  @Test
  @DisplayName("Should return correct required metadata")
  void getRequiredMetadata_ShouldReturnCorrectSet() {
    assertEquals(requiredMetadata, recipe.getRequiredMetadata());
  }

  @Test
  @DisplayName("Should return correct type")
  void getType_ShouldReturnCorrectType() {
    assertEquals(MinionType.USER_DEFINED_AGENT, recipe.getType());
  }

  @Test
  @DisplayName("Should return correct description")
  void getDescription_ShouldReturnCorrectDescription() {
    assertEquals("Test recipe", recipe.getDescription());
  }

  @Test
  @DisplayName("Should return correct requiresTenant flag")
  void isRequiresTenant_ShouldReturnCorrectFlag() {
    assertFalse(recipe.isRequiresTenant());
  }
} 