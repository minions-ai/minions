package com.minionslab.core.api.dto;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePromptRequest {

  @NotNull(message = "Name is required")
  private String name;

  @NotNull(message = "Type is required")
  private MinionType type;

  @NotNull(message = "Version is required")
  @Pattern(regexp = "^\\d+\\.\\d+(\\.\\d+)?$", message = "Version must be in format X.Y or X.Y.Z")
  private String version;

  @NotNull(message = "Tenant ID is required")
  private String tenantId;

  @NotNull(message = "Content is required")
  private String content;

  private Map<String, Object> metadata;

  public MinionPrompt toMinionPrompt() {
    return MinionPrompt.builder()
        .name(name)
        .type(type)
        .version(version)
        .tenantId(tenantId)
        .contents(List.of(content))
        .metadata(metadata)
        .build();
  }
} 