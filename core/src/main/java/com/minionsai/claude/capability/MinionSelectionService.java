package com.minionsai.claude.capability;


import com.minionsai.claude.agent.Minion;
import com.minionsai.claude.agent.factory.MinionFactory;
import com.minionsai.claude.agent.MinionRegistry;
import com.minionsai.claude.workflow.task.Task;
import com.minionsai.claude.prompt.PromptService;
import com.minionsai.claude.prompt.SystemPrompt;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service that manages agent capabilities and helps with agent selection
 */
@Service
@Slf4j
@AllArgsConstructor
public class MinionSelectionService {

  @Autowired
  private final MinionCapabilityStore capabilityStore;

  @Autowired
  private final MinionRegistry minionRegistry;

  @Autowired
  private final MinionFactory minionFactory;

  @Autowired
  private final PromptService promptService;

  /**
   * Register all capabilities for an agent
   */
  public void registerAgentCapabilities(Minion agent, List<MinionCapability> capabilities) {
    String agentType = agent.getClass().getSimpleName();
    capabilityStore.registerAgentCapabilities(capabilities, agentType);

    // Update the agent's capabilities list
    agent.setCapabilities(capabilities);
  }

  /**
   * Find the most appropriate agent for a task
   */
  public Optional<Minion> findAgentForTask(Task task) {
    return findAgentForTaskDescription(task.getDescription());
  }

  /**
   * Find or create an agent based on task description
   */
  public Optional<Minion> findAgentForTaskDescription(String taskDescription) {
    // First, try to find an existing agent that can handle the task
    for (Minion agent : minionRegistry.getAllAgents()) {
      if (canAgentHandleTask(agent, taskDescription)) {
        log.info("Found existing agent {} that can handle task",
            agent.getAgentId());
        return Optional.of(agent);
      }
    }

    // If no existing agent can handle it, find the best agent type
    Optional<MinionMatch> bestMatch = capabilityStore.findBestAgentForTask(taskDescription);

    if (bestMatch.isPresent()) {
      MinionMatch match = bestMatch.get();
      log.info("Found agent type {} for task with score {}",
          match.getAgentType(), match.getScore());

      // Create a new agent of the appropriate type
      try {
        // Get the full prompt from MongoDB
        Optional<SystemPrompt> prompt = promptService.getLatestPromptForAgentType(match.getAgentType());

        if (prompt.isPresent()) {
          // Create a new agent using the prompt
          Minion newAgent = minionFactory.createAgent(match.getAgentType(), prompt.get());

          // Register the new agent
          minionRegistry.registerAgent(newAgent);

          log.info("Created new agent {} of type {}",
              newAgent.getAgentId(), match.getAgentType());

          return Optional.of(newAgent);
        } else {
          log.warn("No prompt found for agent type {}", match.getAgentType());
        }
      } catch (Exception e) {
        log.error("Failed to create agent of type {}", match.getAgentType(), e);
      }
    }

    log.warn("No suitable agent found for task description: {}", taskDescription);
    return Optional.empty();
  }

  /**
   * Check if an agent can handle a specific task
   */
  private boolean canAgentHandleTask(Minion agent, String taskDescription) {
    // Check if any of the agent's capabilities can handle this task
    return agent.getCapabilities().stream()
        .anyMatch(capability -> {
          // Check if the task description is related to this capability
          return capability.isAvailable() &&
              (capability.getDescription().toLowerCase().contains(taskDescription.toLowerCase()) ||
                  taskDescription.toLowerCase().contains(capability.getName().toLowerCase()));
        });
  }

  /**
   * Suggest agents for a task and provide their matching scores
   */
  public List<MinionSuggestion> suggestAgentsForTask(String taskDescription, int maxSuggestions) {
    List<MinionMatch> matches = capabilityStore.findAgentForTask(taskDescription, maxSuggestions);

    if (matches.isEmpty()) {
      //todo: Create a new agent that matches this request.
    }

    List<MinionSuggestion> suggestions = new ArrayList<>();
    for (MinionMatch match : matches) {
      // For each match, check if we already have an active agent of this type
      Minion existingAgent = minionRegistry.getAgentsByType(match.getAgentType())
          .stream()
          .findFirst()
          .orElse(null);

      MinionSuggestion suggestion = new MinionSuggestion(
          match.getAgentType(),
          match.getCapabilityName(),
          match.getScore(),
          existingAgent != null ? existingAgent.getAgentId() : null,
          match.getMatchedCapabilityDescription()
      );

      suggestions.add(suggestion);
    }

    return suggestions;
  }
}
