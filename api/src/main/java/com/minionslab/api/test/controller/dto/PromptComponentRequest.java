package com.minionslab.api.test.controller.dto;

import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(chain = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PromptComponentRequest {

  @NotNull(message = "Content is required")
  private String content;

  private String embeddingId;

  @Default
  private double weight = 1.0;

  @Default
  private double order = 0.0;

  @NotNull(message = "PromptType is required")
  private PromptType type;

  @Singular
  private Map<String, Object> metadatas = new HashMap<>();

  public PromptComponent toPromptComponent() {
    return PromptComponent.builder()
        .text(content)
        .embeddingId(embeddingId)
        .weight(weight)
        .order(order)
        .type(type)
        .metadata(metadatas)
        .build();
  }
}
