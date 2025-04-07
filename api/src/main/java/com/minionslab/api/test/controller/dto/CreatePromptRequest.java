package com.minionslab.api.test.controller.dto;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.MinionType;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(chain = true)
@SuperBuilder
@NoArgsConstructor
public class CreatePromptRequest {

  @NotBlank(message = "Description is required")
  private String description;

  @NotBlank
  private String entityId;

  private Instant effectiveDate;
  private Instant expiryDate;

  @Default
  private Map<String, Object> metadata = new HashMap<>();

  @Default
  private List<PromptComponentRequest> components = new ArrayList<>();

  private String version;

  private String tenantId;

  private MinionType minionType;

  @Default
  private Set<String> toolboxes = new HashSet<>();

  public MinionPrompt toMinionPrompt() {
    return MinionPrompt.builder()
        .entityId(entityId)
        .description(description)
        .effectiveDate(effectiveDate != null ? effectiveDate : Instant.now())
        .expiryDate(expiryDate)
        .toolboxes(toolboxes)
        .components(components.stream()
            .collect(Collectors.toMap(
                PromptComponentRequest::getType,
                component -> PromptComponent.builder()
                    .type(component.getType())
                    .text(component.getContent())
                    .metadata(component.getMetadatas())
                    .build()
            )))
        .metadata(metadata)
        .version(version)
        .build();
  }
} 