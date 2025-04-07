package com.minionslab.api.test.controller.dto;

import static com.minionslab.core.test.TestConstants.TEST_METADATA_KEY;
import static com.minionslab.core.test.TestConstants.TEST_METADATA_VALUE;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_VERSION;
import static com.minionslab.core.test.TestConstants.getMinionPrompt;
import static org.assertj.core.api.Assertions.assertThat;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.test.TestConstants;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdatePromptRequestTest {

  private UpdatePromptRequest updateRequest;


  @BeforeEach
  void setUp() {
    // Initialize the update request
    updateRequest = new UpdatePromptRequest();


  }

  @Test
  void updateMinionPrompt_WithNewContent_ShouldUpdateContentOnly() {
    // Act
    MinionPrompt updatedPrompt = updateRequest.updateMinionPrompt(getMinionPrompt());

    // Assert
    assertThat(updatedPrompt)
        .satisfies(prompt -> {
          assertThat(prompt.getId()).isEqualTo(getMinionPrompt().getId());
          assertThat(prompt.getDescription()).isEqualTo(getMinionPrompt().getDescription());
          assertThat(prompt.getVersion()).isEqualTo(getMinionPrompt().getVersion());
          assertThat(prompt.getTenantId()).isEqualTo(getMinionPrompt().getTenantId());
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
    MinionPrompt updatedPrompt = updateRequest.updateMinionPrompt(getMinionPrompt());

    // Assert
    assertThat(updatedPrompt)
        .satisfies(prompt -> {
          assertThat(prompt.getMetadata())
              .containsEntry(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE)
              .containsEntry(TestConstants.TEST_METADATA_KEY_2, TestConstants.TEST_METADATA_VALUE_2);

        });
  }


  @Test
  void updateMinionPrompt_ShouldMergeMetadata() {
    // Arrange
    MinionPrompt existingPrompt = MinionPrompt.builder()
        .id("test-id")
        .description(TEST_PROMPT_DESCRIPTION)
        .version(TEST_PROMPT_VERSION)
        .metadata(Map.of(TEST_METADATA_KEY, TEST_METADATA_VALUE)) // Uses @Singular
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
    MinionPrompt updatedPrompt = updateRequest.updateMinionPrompt(getMinionPrompt());

    // Assert
    assertThat(updatedPrompt.getMetadata()).isEmpty();
  }

  @Test
  void updateMinionPrompt_WithMultipleContents_ShouldUpdateAllContents() {

    //Fill the body of this method with the required code to fullfill the test

  }

  @Test
  void updateMinionPrompt_ShouldNotModifyOriginalPrompt() {
    // Act
    MinionPrompt updatedPrompt = updateRequest.updateMinionPrompt(getMinionPrompt());

    // Assert

    assertThat(getMinionPrompt()).isNotSameAs(updatedPrompt);
  }
} 