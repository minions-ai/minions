package com.minionslab.core.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Registry for tracking and managing all active agent instances
 */
@Service
@Slf4j
public class MinionRegistry {

  /**
   * Map of agent IDs to agent instances
   */
  private final Map<String, Minion> minionsById = new ConcurrentHashMap<>();
  //  private final Map<MinionType, Minion> minionsByType = new ConcurrentHashMap<>();

  // Counter for generating unique names
  private final AtomicInteger nameCounter = new AtomicInteger(0);


  /**
   * Register an minion in the registry
   *
   * @param minion The minion to register
   */
  public void registerAgent(Minion minion) {
    try {
      // First register in maps to ensure visibility
      minionsById.put(minion.getMinionId(), minion);
//      minionsByType.put(minion.getMinionType(), minion);

      log.info("Registered minion: {} of minionType: {}", minion.getMinionId(), minion.getMinionType());
    } catch (Exception e) {
      // Cleanup on failure
      minionsById.remove(minion.getMinionId());
//      minionsByType.remove(minion.getMinionType());
      log.error("Failed to register minion: {}", minion.getMinionId(), e);
      throw new RuntimeException("Failed to register minion", e);
    }
  }

  private String generateRegistryId(Minion minion) {
    return null;
  }

  /**
   * Get an agent by its ID
   *
   * @param agentId The ID of the agent to retrieve
   * @return The agent instance, or null if not found
   */
  public Minion getMinion(String agentId) {
    Minion agent = minionsById.get(agentId);
    if (agent == null) {
      log.warn("Agent not found: {}", agentId);
    }
    return agent;
  }

  /**
   * Get all registered agents
   *
   * @return List of all agent instances
   */
  public List<Minion> getAllAgents() {
    return new ArrayList<>(minionsById.values());
  }

  /**
   * Get all agents of a specific minionType
   *
   * @param agentType The minionType of agents to retrieve
   * @return List of matching agent instances
   */
  public List<Minion> getAgentsByType(String agentType) {
    return minionsById.values().stream().filter(agent -> agent.getClass().getSimpleName().equals(agentType)).collect(Collectors.toList());
  }

  /**
   * Remove an agent from the registry
   *
   * @param agentId The ID of the agent to remove
   * @return True if the agent was found and removed, false otherwise
   */
  public boolean unregisterAgent(String agentId) {
    Minion minion = minionsById.get(agentId);
    if (minion != null) {
      try {

        // Then remove from maps
        minionsById.remove(agentId);
//        minionsByType.remove(MinionType.valueOf(minion.getMinionType()));

        log.info("Unregistered minion: {} ({})", minion.getMinionId(), agentId);
        return true;
      } catch (Exception e) {
        log.error("Failed to unregister minion: {} ({})", minion.getMinionId(), agentId, e);
        throw new IllegalStateException("Failed to unregister minion", e);
      }
    }
    return false;
  }

  /**
   * Get the number of registered agents
   *
   * @return The count of registered agents
   */
  public int getAgentCount() {
    return minionsById.size();
  }

  /**
   * Get the number of agents by minionType
   *
   * @return Map of agent types to their counts
   */
  public Map<String, Integer> getAgentCounts() {
    Map<String, Integer> counts = new ConcurrentHashMap<>();

    for (Minion agent : minionsById.values()) {
      String type = agent.getClass().getSimpleName();
      counts.put(type, counts.getOrDefault(type, 0) + 1);
    }

    return counts;
  }

  /**
   * Generates a unique name for a minion.
   *
   * @param type The minionType of minion
   * @return A unique name
   */
  public String generateUniqueName(String type) {
    return String.format("%s-%d", type.toLowerCase(), nameCounter.incrementAndGet());
  }

  /**
   * Gets a minion by its ID.
   *
   * @param id The ID of the minion to get
   * @return The minion or null if not found
   */
  public Minion getMinionById(String id) {
    return minionsById.get(id);
  }

  /**
   * Gets a minion by its minionType.
   *
   * @param type The minionType of the minion to get
   * @return The minion or null if not found
   */
  public Minion getMinionByType(String type) {
    return null;
  }


  private String generateRegistryId(String userId, String tenantId, String promptName, String promptVersion) {
    // Create deterministic ID based on user and prompt
    return String.format("%s-%s-%s-%s", tenantId, userId, promptName, promptVersion != null ? promptVersion : "latest");
  }
}