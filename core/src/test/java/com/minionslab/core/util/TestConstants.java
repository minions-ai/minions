package com.minionslab.core.util;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Constants used across test classes to maintain consistency and avoid duplication.
 */
public final class TestConstants {

  // User and Authentication
  public static final String TEST_USER_ID = "test-user";
  public static final String TEST_USERNAME = "testUser";
  public static final String TEST_TENANT_ID = "test-tenant";
  public static final String TEST_ENVIRONMENT_ID = "test-env-id";
  public static final String TEST_ROLE_ADMIN = "ADMIN";
  public static final String TEST_ROLE_USER = "USER";
  // Prompt
  public static final String TEST_PROMPT_ENTITY_ID = "test-prompt";
  public static final String TEST_PROMPT_ID = "test-prompt-id";
  public static final String TEST_PROMPT_DESCRIPTION = "Test prompt description";
  public static final String TEST_PROMPT_UPDATED_DESCRIPTION = "Updated Description";
  public static final String TEST_PROMPT_VERSION = "1.0";
  public static final String TEST_PROMPT_CONTENT = "Test content";
  public static final PromptType TEST_PROMPT_TYPE = PromptType.SYSTEM;
  public static final MinionType TEST_MINION_TYPE = MinionType.TESTING_AGENT;
  // Prompt Components
  public static final String TEST_COMPONENT_ID = "test-component-id";
  public static final String TEST_COMPONENT_TEXT = "Test content";
  public static final String TEST_EMBEDDING_ID = "embed123";
  public static final double TEST_COMPONENT_WEIGHT = 2.0;
  public static final double TEST_COMPONENT_ORDER = 1.0;
  public static final PromptType TEST_COMPONENT_TYPE = PromptType.SYSTEM;
  // Component Types and Texts
  public static final String GUIDELINES_TEXT = "This is a guideline prompt text";
  public static final String CONTEXT_TEXT = "This is a context prompt text";
  public static final String REFLECTION_TEXT = "This is a reflection policy prompt text";
  public static final String POLICY_TEXT = "This is a prompt text";
  public static final String TASK_SPECIFIC_TEXT = "This is a task specific prompt text";
  public static final String USER_TEMPLATE_TEXT = "This is a user template specific prompt text";
  // Metadata
  public static final String TEST_METADATA_KEY = "key1";
  public static final String TEST_METADATA_VALUE = "value1";
  // MongoDB Test Configuration
  public static final String TEST_MONGODB_HOST = "localhost";
  public static final int TEST_MONGODB_PORT = 27017;
  public static final String TEST_MONGODB_DATABASE = "test_db";
  // Validation Messages
  public static final String ERROR_CONTENT_REQUIRED = "Content is required";
  public static final String ERROR_PROMPT_TYPE_REQUIRED = "PromptType is required";
  public static final String ERROR_PROMPT_NULL = "Prompt cannot be null";
  public static final String ERROR_PROMPT_NAME_EMPTY = "Prompt name cannot be empty";
  public static final String ERROR_PROMPT_TYPE_NULL = "Prompt type cannot be null";
  public static final String ERROR_PROMPT_VERSION_EMPTY = "Prompt version cannot be empty";
  public static final String ERROR_METADATA_KEY_NULL = "Metadata key cannot be null";
  public static final String TEST_METADATA_KEY_2 = "key2";
  public static final Object TEST_METADATA_VALUE_2 = 123;
  public static final String TEST_MINION_ID = "test-minion-id";

  private TestConstants() {
    // Prevent instantiation
  }

  public static @NotNull PromptComponent getPromptComponent(PromptType type, String text) {
    return PromptComponent.builder()
        .type(type)
        .text(text)
        .build();
  }

  public static @NotNull Map.Entry<PromptType, PromptComponent> getPromptComponentEntry(PromptType type, String promptText) {
    PromptComponent promptComponent = getPromptComponent(type, promptText);
    return Map.entry(promptComponent.getType(), promptComponent);
  }

  public static @NotNull MinionPrompt getMinionPrompt() {
    return
        MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version(TEST_PROMPT_VERSION)
            .tenantId(TEST_TENANT_ID)
            .build();
  }
}