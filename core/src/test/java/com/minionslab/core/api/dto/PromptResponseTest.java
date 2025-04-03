package com.minionslab.core.api.dto;

import static com.minionslab.core.test.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_ID;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_VERSION;
import static com.minionslab.core.test.TestConstants.TEST_TENANT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.test.TestConstants;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PromptResponseTest {

    @Test
    void fromMinionPrompt_WithValidPrompt_ShouldCreateResponse() {
        // Arrange
        MinionPrompt prompt = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version(TEST_PROMPT_VERSION)
            .tenantId(TEST_TENANT_ID)
            .components(new HashMap<>())
            .metadata(new HashMap<>())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        // Act
        PromptResponse response = PromptResponse.fromMinionPrompt(prompt);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_PROMPT_ID);
        assertThat(response.getVersion()).isEqualTo(TEST_PROMPT_VERSION);
        assertThat(response.getTenantId()).isEqualTo(TEST_TENANT_ID);
        assertThat(response.getMetadata()).isEmpty();
    }

    @Test
    void fromMinionPrompt_WithNullPrompt_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> PromptResponse.fromMinionPrompt(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(TestConstants.ERROR_PROMPT_NULL);
    }

    @Test
    void fromMinionPrompt_WithComponents_ShouldMapComponents() {
        // Arrange
        Map<PromptType, PromptComponent> components = new HashMap<>();
        components.put(TestConstants.TEST_COMPONENT_TYPE, PromptComponent.builder()
            .text(TestConstants.TEST_COMPONENT_TEXT)
            .type(TestConstants.TEST_COMPONENT_TYPE)
            .build());

        MinionPrompt prompt = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version(TEST_PROMPT_VERSION)
            .tenantId(TEST_TENANT_ID)
            .components(components)
            .metadata(new HashMap<>())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        // Act
        PromptResponse response = PromptResponse.fromMinionPrompt(prompt);

        // Assert
        assertThat(response.getComponents()).hasSize(1);
        assertThat(response.getComponents().get(0).getContent()).isEqualTo(TestConstants.TEST_COMPONENT_TEXT);
        assertThat(response.getComponents().get(0).getType()).isEqualTo(TestConstants.TEST_COMPONENT_TYPE);
    }

    @Test
    void fromMinionPrompt_WithMetadata_ShouldMapMetadata() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE);

        MinionPrompt prompt = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version(TEST_PROMPT_VERSION)
            .tenantId(TEST_TENANT_ID)
            .components(new HashMap<>())
            .metadata(metadata)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        // Act
        PromptResponse response = PromptResponse.fromMinionPrompt(prompt);

        // Assert
        assertThat(response.getMetadata()).hasSize(1);
        assertThat(response.getMetadata()).containsEntry(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE);
    }
} 