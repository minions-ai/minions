package com.minionslab.core.api.dto;

import com.minionslab.core.domain.MinionPrompt;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromptResponse {

  private String id;
  private String entityId;
  private String description;
  private String version;
  private String tenantId;
  private List<PromptComponentResponse> components;
  private Map<String, Object> metadata;
  private Instant effectiveDate;
  private Instant expiryDate;
  private Instant createdAt;
  private Instant updatedAt;
  private boolean deployed;
  private Set<String> toolboxes;
  private String error;

  public static PromptResponse fromMinionPrompt(MinionPrompt prompt) {
    if (prompt == null) {
      throw new IllegalArgumentException("Prompt cannot be null");
    }

    // Convert components to PromptComponentResponse
    List<PromptComponentResponse> components = prompt.getComponents().values().stream()
        .map(component -> PromptComponentResponse.builder()
            .type(component.getType())
            .content(component.getText())
            .embeddingId(component.getEmbeddingId())
            .weight(component.getWeight())
            .order(component.getOrder())
            .metadata(component.getMetadata())
            .build())
        .toList();

    return PromptResponse.builder()
        .id(prompt.getId())
        .entityId(prompt.getEntityId())
        .description(prompt.getDescription())
        .version(prompt.getVersion())
        .tenantId(prompt.getTenantId())
        .components(components)
        .metadata(prompt.getMetadata() != null ? new HashMap<>(prompt.getMetadata()) : new HashMap<>()) // Create defensive copy
        .effectiveDate(prompt.getEffectiveDate())
        .expiryDate(prompt.getExpiryDate())
        .createdAt(prompt.getCreatedAt())
        .updatedAt(prompt.getUpdatedAt())
        .deployed(prompt.isDeployed())
        .toolboxes(prompt.getToolboxes())
        .build();
  }
} 