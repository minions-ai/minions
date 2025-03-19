package com.minionslab.core.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdatePromptRequestTest {

  private UpdatePromptRequest updateRequest;
  private MinionPrompt existingPrompt;

  @BeforeEach
  void setUp() {
    // Initialize the update request
    updateRequest = new UpdatePromptRequest();
    updateRequest.setContent("Updated content");

    // Initialize existing prompt using builder with @Singular
    existingPrompt = MinionPrompt.builder()
        .id("test-id")
        .name("Test Prompt")
        .type(MinionType.USER_DEFINED_AGENT)
        .version("1.0")
        .tenantId("test-tenant")
        .contents(List.of("Original content"))  // Uses @Singular
        .build();
  }

  @Test
  void updateMinionPrompt_WithNewContent_ShouldUpdateContentOnly() {
    // Act
    MinionPrompt updatedPrompt = updateRequest.updateMinionPrompt(existingPrompt);

    // Assert
    assertThat(updatedPrompt)
        .satisfies(prompt -> {
          assertThat(prompt.getId()).isEqualTo(existingPrompt.getId());
          assertThat(prompt.getName()).isEqualTo(existingPrompt.getName());
          assertThat(prompt.getType()).isEqualTo(existingPrompt.getType());
          assertThat(prompt.getVersion()).isEqualTo(existingPrompt.getVersion());
          assertThat(prompt.getTenantId()).isEqualTo(existingPrompt.getTenantId());
          assertThat(prompt.getContents()).contains("Updated content");
          assertThat(prompt.getContents()).hasSize(1);
        });
  }

  @Test
  void updateMinionPrompt_WithNewMetadata_ShouldUpdateMetadata() {
    // Arrange
    Map<String, Object> newMetadata = new HashMap<>();
    newMetadata.put("key1", "value1");
    newMetadata.put("key2", 123);
    updateRequest.setMetadata(newMetadata);

    // Act
    MinionPrompt updatedPrompt = updateRequest.updateMinionPrompt(existingPrompt);

    // Assert
    assertThat(updatedPrompt)
        .satisfies(prompt -> {
          assertThat(prompt.getMetadata())
              .containsEntry("key1", "value1")
              .containsEntry("key2", 123);
          assertThat(prompt.getContents()).hasSize(1);
          assertThat(prompt.getContents()).contains("Updated content");
        });
  }

  @Test
  void updateMinionPrompt_WithNullMetadata_ShouldKeepExistingMetadata() {
    // Arrange
    existingPrompt = MinionPrompt.builder()
        .id("test-id")
        .name("Test Prompt")
        .type(MinionType.USER_DEFINED_AGENT)
        .version("1.0")
        .metadata(Map.of("existing", "value"))  // Uses @Singular
        .build();
    updateRequest.setMetadata(null);

    // Act
    MinionPrompt updatedPrompt = updateRequest.updateMinionPrompt(existingPrompt);

    // Assert
    assertThat(updatedPrompt.getMetadata())
        .containsEntry("existing", "value")
        .hasSize(1);
  }

  @Test
  void updateMinionPrompt_ShouldMergeMetadata() {
    // Arrange
    existingPrompt = MinionPrompt.builder()
        .id("test-id")
        .name("Test Prompt")
        .type(MinionType.USER_DEFINED_AGENT)
        .version("1.0")
        .metadata(Map.of("existing", "value")) // Uses @Singular
        .build();

    Map<String, Object> newMetadata = new HashMap<>();
    newMetadata.put("new", "newValue");
    updateRequest.setMetadata(newMetadata);

    // Act
    MinionPrompt updatedPrompt = updateRequest.updateMinionPrompt(existingPrompt);

    // Assert
    assertThat(updatedPrompt.getMetadata())
        .containsEntry("existing", "value")
        .containsEntry("new", "newValue")
        .hasSize(2);
  }

  @Test
  void updateMinionPrompt_WithEmptyMetadata_ShouldKeepEmptyMetadata() {
    // Arrange
    Map<String, Object> emptyMetadata = new HashMap<>();
    updateRequest.setMetadata(emptyMetadata);

    // Act
    MinionPrompt updatedPrompt = updateRequest.updateMinionPrompt(existingPrompt);

    // Assert
    assertThat(updatedPrompt.getMetadata()).isEmpty();
  }

  @Test
  void updateMinionPrompt_WithMultipleContents_ShouldUpdateAllContents() {
    // Arrange
    existingPrompt = MinionPrompt.builder()
        .id("test-id")
        .name("Test Prompt")
        .type(MinionType.USER_DEFINED_AGENT)
        .version("1.0")
        .contents(List.of("First content", "Second content"))
        .build();

    // Act
    MinionPrompt updatedPrompt = updateRequest.updateMinionPrompt(existingPrompt);

    // Assert
    assertThat(updatedPrompt.getContents()).contains("Updated content");
    assertThat(updatedPrompt.getContents())
        .hasSize(1)
        .containsExactly("Updated content");
  }

  @Test
  void updateMinionPrompt_ShouldNotModifyOriginalPrompt() {
    // Act
    MinionPrompt updatedPrompt = updateRequest.updateMinionPrompt(existingPrompt);

    // Assert
    assertThat(existingPrompt.getContents()).contains("Original content");
    assertThat(updatedPrompt.getContents()).contains("Updated content");
    assertThat(existingPrompt).isNotSameAs(updatedPrompt);
  }
} 