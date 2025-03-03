package com.minionsai.claude.context;


import com.minionsai.claude.agent.Minion;
import com.minionsai.claude.workflow.task.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves and manages hierarchical contexts for agents This allows for nested contexts (global → task → subtask) with inheritance
 */
@Component
@Slf4j
public class ContextHierarchyResolver {

  // Cache of context hierarchies by ID
  private final Map<String, ContextNode> contextRegistry = new ConcurrentHashMap<>();

  // Global root context
  private final ContextNode rootContext;

  /**
   * Constructor initializes root context
   */
  public ContextHierarchyResolver() {
    // Create the root context
    this.rootContext = new ContextNode(
        "root",
        null,
        MinionContext.builder().build(),
        ContextNodeType.GLOBAL
    );

    // Register the root context
    contextRegistry.put(rootContext.getId(), rootContext);
  }

  /**
   * Creates a new context node and adds it to the hierarchy
   *
   * @param id       Unique identifier for this context
   * @param parentId Parent context ID (or null for root-level contexts)
   * @param context  The agent context to associate
   * @param type     The type of context node
   * @return The created context node
   */
  public ContextNode createContext(String id, String parentId, MinionContext context, ContextNodeType type) {
    // Find parent context
    ContextNode parentNode = parentId != null
        ? contextRegistry.get(parentId)
        : rootContext;

    if (parentId != null && parentNode == null) {
      throw new IllegalArgumentException("Parent context not found: " + parentId);
    }

    // Create new context node
    ContextNode newNode = new ContextNode(id, parentNode, context, type);

    // Register in context hierarchy
    contextRegistry.put(id, newNode);

    // Add as child to parent
    if (parentNode != null) {
      parentNode.addChild(newNode);
    }

    log.debug("Created context: {} with parent: {}", id, parentId);
    return newNode;
  }

  /**
   * Creates a task context as a child of another context
   */
  public ContextNode createTaskContext(Task task, String parentContextId) {
    MinionContext taskContext = MinionContext.builder()
        .parameters(
            Map.of("taskId", task.getTaskId(),
                "taskType", task.getType(),
                "taskDescription", task.getDescription(),
                "taskCreated", task.getCreationTime())).build();

    // Add task parameters
    task.getParameters().forEach(taskContext::addParameter);

    return createContext(
        "task-" + task.getTaskId(),
        parentContextId,
        taskContext,
        ContextNodeType.TASK
    );
  }

  /**
   * Creates an agent context as a child of a task context
   */
  public ContextNode createAgentContext(Minion agent, String taskContextId) {
    MinionContext minionContext = MinionContext.builder()
        .parameters(
            Map.of("agentId", agent.getAgentId(),
                "agentName", agent.getName(),
                "agentType", agent.getClass().getSimpleName())).build();

    return createContext(
        "agent-" + agent.getAgentId() + "-for-task-" + taskContextId,
        taskContextId,
        minionContext,
        ContextNodeType.AGENT
    );
  }

  /**
   * Gets a context node by ID
   */
  public Optional<ContextNode> getContext(String contextId) {
    return Optional.ofNullable(contextRegistry.get(contextId));
  }

  /**
   * Resolves a parameter value from the context hierarchy
   *
   * @param contextId     The context ID to start resolution from
   * @param parameterName The name of the parameter to resolve
   * @param resolverType  The resolution strategy to use
   * @return Optional containing the resolved value, or empty if not found
   */
  @SuppressWarnings("unchecked")
  public <T> Optional<T> resolveParameter(String contextId, String parameterName, ResolverType resolverType) {
    ContextNode node = contextRegistry.get(contextId);
    if (node == null) {
      log.warn("Context not found for resolution: {}", contextId);
      return Optional.empty();
    }

    switch (resolverType) {
      case NEAREST_MATCH:
        return resolveNearestMatch(node, parameterName);

      case COMPOSITE:
        return (Optional<T>) resolveComposite(node, parameterName);

      case ROOT_ONLY:
        return (Optional<T>) Optional.ofNullable(rootContext.getContext().getParameter(parameterName));

      case LEAF_ONLY:
        return (Optional<T>) Optional.ofNullable(node.getContext().getParameter(parameterName));

      case MERGE_UP:
        return (Optional<T>) resolveMergeUp(node, parameterName);

      default:
        return Optional.empty();
    }
  }

  /**
   * Resolves by finding the nearest match up the context hierarchy
   */
  @SuppressWarnings("unchecked")
  private <T> Optional<T> resolveNearestMatch(ContextNode startNode, String parameterName) {
    ContextNode current = startNode;
    while (current != null) {
      Object value = current.getContext().getParameter(parameterName);
      if (value != null) {
        return Optional.of((T) value);
      }
      current = current.getParent();
    }
    return Optional.empty();
  }

  /**
   * Resolves by combining values from all contexts in the hierarchy Works for List or Map typed parameters
   */
  private Optional<Object> resolveComposite(ContextNode startNode, String parameterName) {
    List<Object> values = new ArrayList<>();
    Map<String, Object> mapValues = new HashMap<>();

    // Traverse up the hierarchy collecting values
    ContextNode current = startNode;
    while (current != null) {
      Object value = current.getContext().getParameter(parameterName);
      if (value != null) {
        values.add(value);

        // If it's a map, add all entries
        if (value instanceof Map<?, ?>) {
          mapValues.putAll((Map<String, Object>) value);
        }
      }
      current = current.getParent();
    }

    // If we found no values, return empty
    if (values.isEmpty()) {
      return Optional.empty();
    }

    // If the values are maps, return the combined map
    if (!mapValues.isEmpty()) {
      return Optional.of(mapValues);
    }

    // Otherwise return the list of values
    return Optional.of(values);
  }

  /**
   * Resolves by merging values from leaf to root Works for Map typed parameters
   */
  private Optional<Object> resolveMergeUp(ContextNode startNode, String parameterName) {
    Map<String, Object> result = new HashMap<>();

    // Start from root and apply each level's values
    List<ContextNode> nodePath = new ArrayList<>();

    // Build path from root to leaf
    ContextNode current = startNode;
    while (current != null) {
      nodePath.add(0, current); // Add to beginning of list
      current = current.getParent();
    }

    // Apply values from root to leaf (so leaf values override root values)
    for (ContextNode node : nodePath) {
      Object value = node.getContext().getParameter(parameterName);
      if (value instanceof Map<?, ?>) {
        result.putAll((Map<String, Object>) value);
      }
    }

    return result.isEmpty() ? Optional.empty() : Optional.of(result);
  }

  /**
   * Transforms the context into a flat map for exporting
   */
  public Map<String, Object> exportFlatContext(String contextId, ResolverType resolverType) {
    Map<String, Object> result = new HashMap<>();

    ContextNode node = contextRegistry.get(contextId);
    if (node == null) {
      return result;
    }

    // Get all parameter names from the context and its parents
    Set<String> parameterNames = new HashSet<>();

    // Collect parameter names
    ContextNode current = node;
    while (current != null) {
      parameterNames.addAll(current.getContext().getParameterNames());
      current = current.getParent();
    }

    // Resolve each parameter
    for (String paramName : parameterNames) {
      resolveParameter(contextId, paramName, resolverType)
          .ifPresent(value -> result.put(paramName, value));
    }

    return result;
  }

  /**
   * Removes a context and all its children from the hierarchy
   */
  public boolean removeContext(String contextId) {
    ContextNode node = contextRegistry.get(contextId);
    if (node == null) {
      return false;
    }

    // Cannot remove root context
    if (node == rootContext) {
      return false;
    }

    // First remove all children recursively
    for (ContextNode child : new ArrayList<>(node.getChildren())) {
      removeContext(child.getId());
    }

    // Remove from parent's children
    if (node.getParent() != null) {
      node.getParent().removeChild(node);
    }

    // Remove from registry
    contextRegistry.remove(contextId);

    log.debug("Removed context: {}", contextId);
    return true;
  }

  /**
   * Applies a function to transform context values
   */
  public void transformContext(String contextId, String parameterName, Function<Object, Object> transformer) {
    ContextNode node = contextRegistry.get(contextId);
    if (node == null) {
      return;
    }

    Object value = node.getContext().getParameter(parameterName);
    if (value != null) {
      Object transformed = transformer.apply(value);
      node.getContext().addParameter(parameterName, transformed);
    }
  }

}