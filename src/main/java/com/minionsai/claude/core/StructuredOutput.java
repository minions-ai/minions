package com.minionsai.claude.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Standard output format that Minions produce after processing a task.
 */
@Getter
@Builder
public class StructuredOutput {
  private final String taskId;

  @Builder.Default
  private final boolean success = true;

  @Singular("dataEntry")
  private final Map<String, Object> data;

  @Singular
  private final List<String> messages;

  @Singular
  private final List<ToolExecution> toolExecutions;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public Object getData(String key) {
    return data.get(key);
  }

  // Helper method to create JSON representation
  public String toJson() throws Exception {
    return objectMapper.writeValueAsString(this);
  }
}