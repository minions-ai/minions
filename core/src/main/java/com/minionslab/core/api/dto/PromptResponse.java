package com.minionslab.core.api.dto;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromptResponse {

  private String id;
  private String name;
  private MinionType type;
  private String version;
  private String tenantId;
  private List<String> content;
  private Map<String, Object> metadata;
  private Instant createdAt;
  private Instant updatedAt;

  public static PromptResponse fromMinionPrompt(MinionPrompt prompt) {
    return PromptResponse.builder()
        .id(prompt.getId())
        .name(prompt.getName())
        .type(prompt.getType())
        .version(prompt.getVersion())
        .tenantId(prompt.getTenantId())
        .content(prompt.getContents())
        .metadata(prompt.getMetadata())
        .createdAt(prompt.getCreatedAt())
        .updatedAt(prompt.getUpdatedAt())
        .build();
  }
} 