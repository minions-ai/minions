package com.minionsai.claude.core;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ToolResult {
  @Builder.Default
  private final boolean success = true;

  private final Object data;
  private final String errorMessage;
}