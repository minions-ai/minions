package com.minionsai.claude.prompt;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single component/section within a system prompt
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor public class PromptComponent {

  private String id;
  private String name;

  private String content;

  /**
   * Embedding vector ID for this component (when stored in vector DB)
   */
  private String embeddingId;

  /**
   * Weight of this component (used for vector search)
   */
  @Builder.Default private double weight = 1.0;

  /**
   * Order in which this component should appear (lower values first)
   */
  @Builder.Default private double order = 0.0;

  /**
   * Component type (e.g., "instruction", "context", "examples", etc.)
   */
  private PromptType type;

  /**
   * Additional metadata for this component
   */
  @Builder.Default private Map<String, Object> metadata = new HashMap<>();


  /**
   * Converts a PromptComponent to a formatted text string for the model
   */
  public String getFullPromptText() {
    StringBuilder formatted = new StringBuilder();

    // Add section header based on component type
    if (this.getType() != null) {
      formatted.append("<").append(this.getType().name()).append(">\n");
    }

    // Add component name if available
    if (this.getName() != null && !this.getName().isEmpty()) {
      formatted.append("# ").append(this.getName()).append("\n\n");
    }

    // Add the main content
    formatted.append(this.getContent()).append("\n");

    // Close the section
    if (this.getType() != null) {
      formatted.append("</").append(this.getType().name()).append(">\n");
    }

    return formatted.toString();
  }
}
