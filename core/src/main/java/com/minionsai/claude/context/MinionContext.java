package com.minionsai.claude.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * Represents context information for an agent Contains parameters and metadata for agent execution
 */
@Data
@Builder
public class MinionContext implements Serializable {

  private static final long serialVersionUID = 1L;

  private String agentId;
  private String userId;
  private ContextNodeType ContextType;
  /**
   * Context parameters
   */
  @Builder.Default
  private final Map<String, Object> parameters = new ConcurrentHashMap<>();

  /**
   * Context creation timestamp
   */
  @Builder.Default
  private final long createdAt = System.currentTimeMillis();

  /**
   * Last updated timestamp
   */
  @Builder.Default
  private long updatedAt = System.currentTimeMillis();

  // Add a default constructor
  public MinionContext() {
    this.parameters = new ConcurrentHashMap<>();
    this.createdAt = System.currentTimeMillis();
    this.updatedAt = System.currentTimeMillis();
  }

  // Add a constructor with the expected parameters
  public MinionContext(String agentId, String userId, ContextNodeType contextType, Map<String, Object> parameters, long createdAt, long updatedAt) {
    this.agentId = agentId;
    this.userId = userId;
    this.ContextType = contextType;
    this.parameters = parameters != null ? parameters : new ConcurrentHashMap<>();
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /**
   * Creates a merged context by combining two contexts
   *
   * @param context1 First context
   * @param context2 Second context (takes precedence on conflicts)
   * @return New merged context
   */
  public static MinionContext merge(MinionContext context1, MinionContext context2) {
    MinionContext merged = MinionContext.builder().build();

    if (context1 != null) {
      merged.addParameters(context1.parameters);
    }

    if (context2 != null) {
      merged.addParameters(context2.parameters);
    }

    return merged;
  }

  /**
   * Adds a parameter to the context
   *
   * @param key   Parameter name
   * @param value Parameter value
   * @return this MinionContext for method chaining
   */
  public MinionContext addParameter(String key, Object value) {
    if (StringUtils.hasText(key) && value != null) {
      parameters.put(key, value);
      updatedAt = System.currentTimeMillis();
    }
    return this;
  }

  /**
   * Adds multiple parameters to the context
   *
   * @param params Map of parameters to add
   * @return this MinionContext for method chaining
   */
  public MinionContext addParameters(Map<String, Object> params) {
    if (params != null && !params.isEmpty()) {
      parameters.putAll(params);
      updatedAt = System.currentTimeMillis();
    }
    return this;
  }

  /**
   * Gets a parameter from the context
   *
   * @param key Parameter name
   * @return Parameter value or null if not found
   */
  @SuppressWarnings("unchecked")
  public <T> T getParameter(String key) {
    return (T) parameters.get(key);
  }

  /**
   * Gets a parameter with a default value if not found
   *
   * @param key          Parameter name
   * @param defaultValue Default value to return if parameter not found
   * @return Parameter value or defaultValue if not found
   */
  @SuppressWarnings("unchecked")
  public <T> T getParameter(String key, T defaultValue) {
    Object value = parameters.get(key);
    return value != null ? (T) value : defaultValue;
  }

  /**
   * Removes a parameter from the context
   *
   * @param key Parameter name
   * @return Previous value or null if not found
   */
  public Object removeParameter(String key) {
    if (parameters.containsKey(key)) {
      updatedAt = System.currentTimeMillis();
      return parameters.remove(key);
    }
    return null;
  }

  /**
   * Checks if a parameter exists
   *
   * @param key Parameter name
   * @return true if parameter exists, false otherwise
   */
  public boolean hasParameter(String key) {
    return parameters.containsKey(key);
  }

  /**
   * Gets all parameter names
   *
   * @return Set of parameter names
   */
  public Set<String> getParameterNames() {
    return parameters.keySet();
  }

  /**
   * Clears all parameters
   */
  public void clearParameters() {
    parameters.clear();
    updatedAt = System.currentTimeMillis();
  }

  /**
   * Creates a snapshot of the current context
   *
   * @return Copy of this context
   */
  public MinionContext createSnapshot() {
    MinionContext snapshot = MinionContext
        .builder()
        .parameters(this.parameters)
        .createdAt(System.currentTimeMillis())
        .build();
    snapshot.addParameters(new HashMap<>(this.parameters));
    return snapshot;
  }

  /**
   * Updates this context from another context
   *
   * @param other     Context to update from
   * @param overwrite Whether to overwrite existing values
   * @return this MinionContext for method chaining
   */
  public MinionContext updateFrom(MinionContext other, boolean overwrite) {
    if (other == null) {
      return this;
    }

    if (overwrite) {
      // Overwrite everything
      addParameters(other.parameters);
    } else {
      // Only add missing parameters
      for (Map.Entry<String, Object> entry : other.parameters.entrySet()) {
        if (!this.parameters.containsKey(entry.getKey())) {
          this.parameters.put(entry.getKey(), entry.getValue());
        }
      }
      updatedAt = System.currentTimeMillis();
    }

    return this;
  }


}
