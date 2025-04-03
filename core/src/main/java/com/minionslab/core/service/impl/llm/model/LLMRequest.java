package com.minionslab.core.service.impl.llm.model;

import com.minionslab.core.domain.MinionPrompt;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LLMRequest {

  private MinionPrompt prompt;
  private String userRequest;

}

