package com.minionsai.claude.context;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;



import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing and retrieving agent contexts
 * Provides centralized access to contexts for multiple agents
 */
@Service
@Slf4j
public class ContextManager extends ContextHierarchyResolver {

  /**
   * In-memory store of active contexts
   */
  private final Map<String, MinionContext> activeContexts = new ConcurrentHashMap<>();

  /**
   * Default TTL for contexts in milliseconds (30 minutes)
   */
  @Value("${minions.context.default-ttl-ms:1800000}")
  private long defaultTtlMs;

  /**
   * Create a new context
   * @param agentId The ID of the agent this context belongs to
   * @param userId The ID of the user or system that initiated the request
   * @param contextType The type of context
   * @return The new context instance
   */
  public MinionContext createContext(String agentId, String userId, String contextType) {
    MinionContext context = MinionContext.builder()
        .agentId(agentId)
        .userId(userId)
        .contextType(contextType)
        .createdAt(Instant.now().toEpochMilli())
        .updatedAt(Instant.now().toEpochMilli())
        .ttlMs(defaultTtlMs)
        .build();

    activeContexts.put(context.getContextId(), context);
    log.debug("Created new context: {} for agent: {}", context.getContextId(), agentId);

    return context;
  }

  /**
   * Get a context by ID
   * @param contextId The ID of the context to retrieve
   * @return Optional containing the context, or empty if not found
   */
  public Optional<MinionContext> getContext(String contextId) {
    MinionContext context = activeContexts.get(contextId);
    if (context == null) {
      log.debug("Context not found: {}", contextId);
      return Optional.empty();
    }

    // Update last access time via a fresh context event
    long now = Instant.now().toEpochMilli();
    context.getHistory().add(MinionContext.ContextEvent.builder()
        .eventType("CONTEXT_ACCESSED")
        .timestamp(now)
        .build());

    return Optional.of(context);
  }

  /**
   * Save or update a context
   * @param context The context to save or update
   * @return The saved context
   */
  public MinionContext saveContext(MinionContext context) {
    context.setUpdatedAt(Instant.now().toEpochMilli());
    activeContexts.put(context.getContextId(), context);
    log.debug("Saved context: {}", context.getContextId());
    return context;
  }

  /**
   * Delete a context
   * @param contextId The ID of the context to delete
   * @return True if the context was found and deleted, false otherwise
   */
  public boolean deleteContext(String contextId) {
    MinionContext removed = activeContexts.remove(contextId);
    if (removed != null) {
      log.debug("Deleted context: {}", contextId);
      return true;
    }
    log.debug("Failed to delete context, not found: {}", contextId);
    return false;
  }

  /**
   * Get all contexts for an agent
   * @param agentId The ID of the agent
   * @return List of contexts for the agent
   */
  public List<MinionContext> getContextsForAgent(String agentId) {
    return activeContexts.values().stream()
        .filter(context -> agentId.equals(context.getAgentId()))
        .collect(Collectors.toList());
  }

  /**
   * Get all contexts for a user
   * @param userId The ID of the user
   * @return List of contexts for the user
   */
  public List<MinionContext> getContextsForUser(String userId) {
    return activeContexts.values().stream()
        .filter(context -> userId.equals(context.getUserId()))
        .collect(Collectors.toList());
  }

  /**
   * Get contexts by tag
   * @param tag The tag to search for
   * @return List of contexts with the specified tag
   */
  public List<MinionContext> getContextsByTag(String tag) {
    return activeContexts.values().stream()
        .filter(context -> context.getTags().contains(tag))
        .collect(Collectors.toList());
  }

  /**
   * Get all contexts of a specific type
   * @param contextType The type of contexts to retrieve
   * @return List of contexts of the specified type
   */
  public List<MinionContext> getContextsByType(String contextType) {
    return activeContexts.values().stream()
        .filter(context -> contextType.equals(context.getContextType()))
        .collect(Collectors.toList());
  }

  /**
   * Get the most recent context for an agent
   * @param agentId The ID of the agent
   * @return Optional containing the most recent context, or empty if none found
   */
  public Optional<MinionContext> getMostRecentContextForAgent(String agentId) {
    return activeContexts.values().stream()
        .filter(context -> agentId.equals(context.getAgentId()))
        .max((c1, c2) -> Long.compare(c1.getUpdatedAt(), c2.getUpdatedAt()));
  }

  /**
   * Create a child context for an existing parent context
   * @param parentContextId The ID of the parent context
   * @param contextType The type of the child context
   * @return The new child context, or null if parent not found
   */
  public MinionContext createChildContext(String parentContextId, String contextType) {
    Optional<MinionContext> parentOpt = getContext(parentContextId);
    if (parentOpt.isEmpty()) {
      log.warn("Cannot create child context, parent not found: {}", parentContextId);
      return null;
    }

    MinionContext parent = parentOpt.get();
    MinionContext childContext = MinionContext.builder()
        .agentId(parent.getAgentId())
        .userId(parent.getUserId())
        .contextType(contextType)
        .parentContextId(parentContextId)
        .createdAt(Instant.now().toEpochMilli())
        .updatedAt(Instant.now().toEpochMilli())
        .ttlMs(parent.getTtlMs())
        .build();

    // Add child to parent
    parent.getChildContexts().add(childContext);

    // Save both contexts
    activeContexts.put(childContext.getContextId(), childContext);
    activeContexts.put(parent.getContextId(), parent);

    log.debug("Created child context: {} for parent: {}",
        childContext.getContextId(), parentContextId);

    return childContext;
  }

  /**
   * Get all child contexts for a parent context
   * @param parentContextId The ID of the parent context
   * @return List of child contexts
   */
  public List<MinionContext> getChildContexts(String parentContextId) {
    return activeContexts.values().stream()
        .filter(context -> parentContextId.equals(context.getParentContextId()))
        .collect(Collectors.toList());
  }

  /**
   * Resolve a context by ID (implementation of ContextHierarchyResolver)
   */
  @Override
  public MinionContext resolveContext(String contextId) {
    return activeContexts.get(contextId);
  }

  /**
   * Run cleanup task to remove expired contexts
   * Scheduled to run every 5 minutes
   */
  @Scheduled(fixedRate = 300000)
  public void cleanupExpiredContexts() {
    long now = Instant.now().toEpochMilli();
    List<String> expiredContextIds = new ArrayList<>();

    for (MinionContext context : activeContexts.values()) {
      long expiryTime = context.getUpdatedAt() + context.getTtlMs();
      if (now > expiryTime) {
        expiredContextIds.add(context.getContextId());
      }
    }

    for (String contextId : expiredContextIds) {
      activeContexts.remove(contextId);
    }

    if (!expiredContextIds.isEmpty()) {
      log.info("Cleaned up {} expired contexts", expiredContextIds.size());
    }
  }

  /**
   * Set the default TTL for new contexts
   * @param ttlMs Time-to-live in milliseconds
   */
  public void setDefaultTtlMs(long ttlMs) {
    this.defaultTtlMs = ttlMs;
    log.info("Updated default context TTL to {} ms", ttlMs);
  }

  /**
   * Get the number of active contexts
   * @return Count of active contexts
   */
  public int getActiveContextCount() {
    return activeContexts.size();
  }
}
