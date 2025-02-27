package com.minionsai.claude.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Represents a unit of work to be processed by a Minion.
 */
@Getter
@Builder
public class Task {
  @Builder.Default
  private final String id = UUID.randomUUID().toString();

  private final String type;

  @Singular
  private final Map<String, Object> parameters;

  @Builder.Default
  private final TaskPriority priority = TaskPriority.NORMAL;

  private final ZonedDateTime deadline;

  public Object getParameter(String key) {
    return parameters.get(key);
  }

  public boolean hasDeadline() {
    return deadline != null;
  }

  public boolean isOverdue() {
    return hasDeadline() && ZonedDateTime.now().isAfter(deadline);
  }
}