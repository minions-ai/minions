package com.minionslab.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.util.TestConstants;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromptComponentTest {

  @Test
  void builder_WithValidData_ShouldCreateInstance() {
    // Arrange
    Map<String, Object> metadata = new HashMap<>();
    metadata.put(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE);

    // Act
    PromptComponent component = PromptComponent.builder()
        .text(TestConstants.TEST_COMPONENT_TEXT)
        .embeddingId(TestConstants.TEST_EMBEDDING_ID)
        .weight(TestConstants.TEST_COMPONENT_WEIGHT)
        .order(TestConstants.TEST_COMPONENT_ORDER)
        .type(TestConstants.TEST_COMPONENT_TYPE)
        .metadata(metadata)
        .build();

    // Assert
    assertThat(component).isNotNull();
    assertThat(component.getText()).isEqualTo(TestConstants.TEST_COMPONENT_TEXT);
    assertThat(component.getEmbeddingId()).isEqualTo(TestConstants.TEST_EMBEDDING_ID);
    assertThat(component.getWeight()).isEqualTo(TestConstants.TEST_COMPONENT_WEIGHT);
    assertThat(component.getOrder()).isEqualTo(TestConstants.TEST_COMPONENT_ORDER);
    assertThat(component.getType()).isEqualTo(TestConstants.TEST_COMPONENT_TYPE);
    assertThat(component.getMetadata()).containsEntry(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE);
  }

  @Test
  void builder_WithDefaultValues_ShouldUseDefaults() {
    // Act
    PromptComponent component = PromptComponent.builder().build();

    // Assert
    assertThat(component).isNotNull();
    assertThat(component.getWeight()).isEqualTo(1.0);
    assertThat(component.getOrder()).isEqualTo(0.0);
    assertThat(component.getMetadata()).isEmpty();
    assertThat(component.getId()).isNotNull();
  }

  @Test
  void getJsonContent_WithValidData_ShouldReturnValidJson() {
    // Arrange
    PromptComponent component = PromptComponent.builder()
        .id(TestConstants.TEST_COMPONENT_ID)
        .text(TestConstants.TEST_COMPONENT_TEXT)
        .type(TestConstants.TEST_COMPONENT_TYPE)
        .weight(TestConstants.TEST_COMPONENT_WEIGHT)
        .order(TestConstants.TEST_COMPONENT_ORDER)
        .build();

    // Act
    String json = component.getJsonContent();

    // Assert
    assertThat(json)
        .contains("\"id\":\"" + TestConstants.TEST_COMPONENT_ID + "\"")
        .contains("\"type\":\"" + TestConstants.TEST_COMPONENT_TYPE + "\"")
        .contains("\"text\":\"" + TestConstants.TEST_COMPONENT_TEXT + "\"")
        .contains("\"weight\":" + TestConstants.TEST_COMPONENT_WEIGHT)
        .contains("\"order\":" + TestConstants.TEST_COMPONENT_ORDER);
  }

  @Test
  void getJsonContent_WithNullText_ShouldHandleNull() {
    // Arrange
    PromptComponent component = PromptComponent.builder()
        .id(TestConstants.TEST_COMPONENT_ID)
        .type(TestConstants.TEST_COMPONENT_TYPE)
        .build();

    // Act
    String json = component.getJsonContent();

    // Assert
    assertThat(json)
        .contains("\"text\":\"\"");
  }

  @Test
  void getFullPromptText_WithValidData_ShouldReturnFormattedText() {
    // Arrange
    PromptComponent component = PromptComponent.builder()
        .text(TestConstants.TEST_COMPONENT_TEXT)
        .type(TestConstants.TEST_COMPONENT_TYPE)
        .build();

    // Act
    String formatted = component.getFullPromptText();

    // Assert
    assertThat(formatted)
        .isEqualTo("<" + TestConstants.TEST_COMPONENT_TYPE + ">\n" + TestConstants.TEST_COMPONENT_TEXT + "\n</" + TestConstants.TEST_COMPONENT_TYPE + ">\n");
  }

  @Test
  void getFullPromptText_WithNullType_ShouldReturnOnlyText() {
    // Arrange
    PromptComponent component = PromptComponent.builder().type(PromptType.DYNAMIC)
        .text(TestConstants.TEST_COMPONENT_TEXT)
        .build();

    // Act
    String formatted = component.getFullPromptText();
    formatted = formatted.replace("\n", "");

    // Assert
    assertThat(formatted).isEqualTo("<DYNAMIC>" + TestConstants.TEST_COMPONENT_TEXT + "</DYNAMIC>");
  }

  @Test
  void getFullPromptText_WithNullText_ShouldReturnEmptyString() {
    // Arrange
    PromptComponent component = PromptComponent.builder()
        .type(TestConstants.TEST_COMPONENT_TYPE)
        .build();

    // Act
    String formatted = component.getFullPromptText();

    // Assert
    assertThat(formatted).isEmpty();
  }

  @Test
  void getRawContent_WithValidData_ShouldReturnTrimmedText() {
    // Arrange
    PromptComponent component = PromptComponent.builder()
        .text("  " + TestConstants.TEST_COMPONENT_TEXT + "  ")
        .build();

    // Act
    String raw = component.getRawContent();

    // Assert
    assertThat(raw).isEqualTo(TestConstants.TEST_COMPONENT_TEXT);
  }

  @Test
  void getRawContent_WithNullText_ShouldReturnEmptyString() {
    // Arrange
    PromptComponent component = PromptComponent.builder().build();

    // Act
    String raw = component.getRawContent();

    // Assert
    assertThat(raw).isEmpty();
  }

  @Test
  void addMetadata_WithValidKey_ShouldAddMetadata() {
    // Arrange
    PromptComponent component = PromptComponent.builder().build();

    // Act
    component.addMetadata(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE);

    // Assert
    assertThat(component.getMetadata())
        .containsEntry(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE);
  }

  @Test
  void addMetadata_WithNullKey_ShouldThrowException() {
    // Arrange
    PromptComponent component = PromptComponent.builder().build();

    // Act & Assert
    assertThatThrownBy(() -> component.addMetadata(null, TestConstants.TEST_METADATA_VALUE))
        .isInstanceOf(NullPointerException.class)
        .hasMessage(TestConstants.ERROR_METADATA_KEY_NULL);
  }


} 