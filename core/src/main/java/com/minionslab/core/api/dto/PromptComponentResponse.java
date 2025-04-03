package com.minionslab.core.api.dto;

import com.minionslab.core.domain.enums.PromptType;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromptComponentResponse {
  private PromptType type;
  private String content;
  private String embeddingId;
  private double weight;
  private double order;
  private Map<String, Object> metadata;
} 