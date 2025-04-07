package com.minionslab.core.service.impl.llm.model;

import com.minionslab.core.domain.MinionPrompt;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(chain = true)
@SuperBuilder
public class LLMRequest {

  @NonNull
  private MinionPrompt prompt;
  @NotNull
  private String userRequest;
  @NonNull
  private String minionId;
  @Default
  private Map<String, Object> metadata = new HashMap<>();

}

