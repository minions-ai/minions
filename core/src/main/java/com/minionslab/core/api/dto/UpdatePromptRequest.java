package com.minionslab.core.api.dto;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePromptRequest {

  @NotNull(message = "Content is required")
  private String content;

  private Map<String, Object> metadata;

  public MinionPrompt updateMinionPrompt(MinionPrompt existingPrompt) {
    Map<String, Object> mergedMetadata = mergeMetadata(existingPrompt.getMetadata());

    MinionPrompt prompt = MinionPrompt.builder()
        .id(existingPrompt.getId())
        .name(existingPrompt.getName())
        .type(existingPrompt.getType())
        .version(existingPrompt.getVersion())
        .tenantId(existingPrompt.getTenantId())
        .build();
    prompt.getMetadata().putAll(mergedMetadata);
    prompt.getContents().add(content);
    return prompt;

  }

  private Map<String, Object> mergeMetadata(Map<String, Object> existingMetadata) {
    if (metadata == null && existingMetadata == null) {
      return null;
    }

    Map<String, Object> result = new HashMap<>();

    // Add existing metadata if present
    if (existingMetadata != null) {
      result.putAll(existingMetadata);
    }

    // Override with new metadata if present
    if (metadata != null) {
      result.putAll(metadata);
    }

    return result;
  }

  private PromptComponent createPromptComponent() {
    return PromptComponent.builder()
        .content(content)
        .build();
  }
} 