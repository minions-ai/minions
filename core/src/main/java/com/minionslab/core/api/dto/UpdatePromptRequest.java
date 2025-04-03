package com.minionslab.core.api.dto;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.MinionPrompt;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import com.minionslab.core.domain.enums.PromptType;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Builder.Default;

@Data
@Accessors(chain = true)
@SuperBuilder
@NoArgsConstructor
public class UpdatePromptRequest {

  @NotBlank(message = "Description is required")
  private String description;

  private Instant effectiveDate;
  private Instant expiryDate;

  @Default
  private Map<String, Object> metadata = new HashMap<>();

  @Default
  private List<PromptComponentRequest> components = new ArrayList<>();

  @Default
  private Set<String> toolboxes = new HashSet<>();

  public MinionPrompt updateMinionPrompt(MinionPrompt prompt) {

    if (description != null) {
      prompt.setDescription(description);
    }
    if (effectiveDate != null) {
      prompt.setEffectiveDate(effectiveDate);
    }
    if (expiryDate != null) {
      prompt.setExpiryDate(expiryDate);
    }

    if (metadata != null) {
      prompt.setMetadata(metadata);
    }
    if (components != null && !components.isEmpty()) {
      Map<PromptType, PromptComponent> newComponents = components.stream()
          .collect(Collectors.toMap(
              PromptComponentRequest::getType,
              component -> PromptComponent.builder()
                  .type(component.getType())
                  .text(component.getContent())
                  .metadata(component.getMetadatas())
                  .build()
          ));
      prompt.setComponents(newComponents);
    }
    if (toolboxes != null) {
      prompt.setToolboxes(toolboxes);
    }
    return prompt;
  }
} 