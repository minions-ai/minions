package com.minionsai.claude.core;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ToolExecution {
  private final String toolId;
  private final Map<String, Object> parameters;
  private final Object result;

  @Builder.Default
  private final boolean successful = true;

  private final String errorMessage;
}