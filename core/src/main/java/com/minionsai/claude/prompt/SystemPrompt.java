package com.minionsai.claude.prompt;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a system prompt that can consist of multiple components/sections This class is designed to be stored in a document database
 * (MongoDB)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "system_prompts")
public class SystemPrompt {

  @Id
  private String id;

  private String name;
  private String description;
  private String agentType;
  private String version;

  @Builder.Default
  private List<PromptComponent> components = new ArrayList<>();

  @Builder.Default
  private Map<String, Object> metadata = new HashMap<>();

  /**
   * Creation timestamp
   */
  private long createdAt;

  /**
   * Last updated timestamp
   */
  private long updatedAt;

  /**
   * Add a new component to this system prompt
   */
  public SystemPrompt addComponent(PromptComponent component) {
    this.components.add(component);
    return this;
  }

  /**
   * Add a new component to this system prompt
   */
  public SystemPrompt addComponent(String name, String content, double weight) {
    PromptComponent component = PromptComponent.builder()
        .id(UUID.randomUUID().toString())
        .name(name)
        .content(content)
        .weight(weight)
        .build();
    return addComponent(component);
  }

  /**
   * Get the complete system prompt text by combining all components
   */
  public String getFullPromptText() {
    StringBuilder builder = new StringBuilder();

    // Sort components by order if specified
    List<PromptComponent> sortedComponents = new ArrayList<>(components);
    sortedComponents.sort((c1, c2) -> Double.compare(c1.getOrder(), c2.getOrder()));

    // Combine all component contents
    for (PromptComponent component : sortedComponents) {
      builder.append(component.getFullPromptText());
      builder.append("\n\n");
    }

    return builder.toString().trim();
  }

}