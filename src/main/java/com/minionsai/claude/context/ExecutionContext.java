package com.minionsai.claude.context;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides runtime contextual information for function/tool execution.
 * Immutable but can create modified copies with the @With annotation.
 */
@Getter
@Builder
public class ExecutionContext {
  @Builder.Default
  private final String executionId = UUID.randomUUID().toString();

  @Builder.Default
  private final ZonedDateTime timestamp = ZonedDateTime.now();

  @With
  private final String userId;

  @With
  private final String sessionId;

  @With
  private final String tenantId;

  @With
  private final Map<String, Object> attributes;

  /**
   * Get an attribute value from the context.
   */
  @SuppressWarnings("unchecked")
  public <T> Optional<T> getAttribute(String key) {
    if (attributes == null || !attributes.containsKey(key)) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable((T) attributes.get(key));
    } catch (ClassCastException e) {
      return Optional.empty();
    }
  }

  /**
   * Create a new context with an additional attribute.
   */
  public ExecutionContext withAttribute(String key, Object value) {
    Map<String, Object> newAttributes = attributes == null ?
        new HashMap<>() : new HashMap<>(attributes);
    newAttributes.put(key, value);
    return withAttributes(newAttributes);
  }

  /**
   * Create a builder initialized with the current values.
   */
  public ExecutionContextBuilder toBuilder() {
    return ExecutionContext.builder()
        .executionId(this.executionId)
        .timestamp(this.timestamp)
        .userId(this.userId)
        .sessionId(this.sessionId)
        .tenantId(this.tenantId)
        .attributes(this.attributes != null ? new HashMap<>(this.attributes) : null);
  }
}