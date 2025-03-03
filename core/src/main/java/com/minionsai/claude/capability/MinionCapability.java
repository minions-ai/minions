package com.minionsai.claude.capability;


import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a capability of an agent This is used to determine if an agent can handle a specific task
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinionCapability {

  /**
   * Unique identifier for this capability
   */
  private String id;

  /**
   * Name of the capability (e.g., "TextSummarization")
   */
  private String name;

  /**
   * Type of the capability (e.g., "NLP", "DataAnalysis", "Reasoning")
   */
  private String type;

  /**
   * Detailed description of what this capability can do This is used for semantic matching in the vector store
   */
  private String description;

  /**
   * Whether this capability is currently available
   */
  @Builder.Default
  private boolean available = true;

  /**
   * Required tools for this capability
   */
  @Builder.Default
  private String[] requiredTools = new String[0];

  /**
   * Requirements and constraints
   */
  @Builder.Default
  private Map<String, Object> requirements = new HashMap<>();

  /**
   * Check if this capability can handle a specific task type
   */
  public boolean canHandle(String taskType) {
    // Default implementation checks if the task type matches the capability name
    // More sophisticated implementations could use the requirements map or other logic
    return name.equalsIgnoreCase(taskType) ||
        description.toLowerCase().contains(taskType.toLowerCase());
  }
}



