package com.minionsai.claude.agent;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
  private final Map<String, Minion> agentsById = new ConcurrentHashMap<>();

  /**
   * Register an agent in the registry
   *
   * @param agent The agent to register
   */
  public void registerAgent(Minion agent) {
    agentsById.put(agent.getAgentId(), agent);
    log.info("Registered agent: {} ({})", agent.getName(), agent.getAgentId());
  }

  /**
   * Get an agent by its ID
   *
   * @param agentId The ID of the agent to retrieve
   * @return The agent instance, or null if not found
   */
  public Minion getAgent(String agentId) {
    Minion agent = agentsById.get(agentId);
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
    return new ArrayList<>(agentsById.values());
  }

  /**
   * Get all agents of a specific type
   *
   * @param agentType The type of agents to retrieve
   * @return List of matching agent instances
   */
  public List<Minion> getAgentsByType(String agentType) {
    return agentsById.values().stream()
        .filter(agent -> agent.getClass().getSimpleName().equals(agentType))
        .collect(Collectors.toList());
  }

  /**
   * Remove an agent from the registry
   *
   * @param agentId The ID of the agent to remove
   * @return True if the agent was found and removed, false otherwise
   */
  public boolean unregisterAgent(String agentId) {
    Minion agent = agentsById.remove(agentId);
    if (agent != null) {
      log.info("Unregistered agent: {}", agentId);
      return true;
    } else {
      log.warn("Failed to unregister agent, not found: {}", agentId);
      return false;
    }
  }

  /**
   * Get the number of registered agents
   *
   * @return The count of registered agents
   */
  public int getAgentCount() {
    return agentsById.size();
  }

  /**
   * Get the number of agents by type
   *
   * @return Map of agent types to their counts
   */
  public Map<String, Integer> getAgentCounts() {
    Map<String, Integer> counts = new ConcurrentHashMap<>();

    for (Minion agent : agentsById.values()) {
      String type = agent.getClass().getSimpleName();
      counts.put(type, counts.getOrDefault(type, 0) + 1);
    }

    return counts;
  }
}