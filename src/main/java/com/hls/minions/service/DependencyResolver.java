package com.hls.minions.service;

import com.hls.minions.model.Agent;
import com.hls.minions.model.Agent;
import com.hls.minions.model.AgentState;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// DependencyResolver class for managing agent dependencies
@Slf4j
public class DependencyResolver {

    // Validate that the dependency graph is acyclic
    public boolean isDependencyGraphValid(List<Agent> agents) {
        try {
            List<Agent> visited = new ArrayList<>();
            for (Agent agent : agents) {
                checkForCycles(agent, visited, new ArrayList<>());
            }
            return true;
        } catch (IllegalStateException e) {
            log.error("Invalid dependency graph: {}", e.getMessage());
            return false;
        }
    }

    // Recursive method to check for cycles
    private void checkForCycles(Agent agent, List<Agent> visited, List<Agent> stack) {
        if (stack.contains(agent)) {
            throw new IllegalStateException("Circular dependency detected for agent: " + agent.getName());
        }
        if (!visited.contains(agent)) {
            stack.add(agent);
            for (Agent dependency : agent.getDependencies()) {
                checkForCycles(dependency, visited, stack);
            }
            stack.remove(agent);
            visited.add(agent);
        }
    }

    // Get agents that are ready for execution
    public List<Agent> getExecutableAgents(List<Agent> agents) {
        return agents.stream()
                .filter(agent -> agent.getState() == AgentState.PENDING && agent.areDependenciesResolved())
                .collect(Collectors.toList());
    }

    // Mark a agent as completed and update dependents
    public void markAgentCompleted(Agent completedAgent, List<Agent> agents) {
        agents.stream()
                .filter(agent -> agent.getDependencies().contains(completedAgent))
                .forEach(agent -> {
                    if (agent.areDependenciesResolved()) {
                        log.error("Agent ready for execution: {}", agent.getName());
                    }
                });
    }

    // Determine execution order using topological sort
    public List<Agent> determineExecutionOrder(List<Agent> agents) {
        List<Agent> sortedAgents = new ArrayList<>();
        List<Agent> visited = new ArrayList<>();

        for (Agent agent : agents) {
            topologicalSort(agent, visited, sortedAgents);
        }

        return sortedAgents;
    }

    // Recursive topological sort
    private void topologicalSort(Agent agent, List<Agent> visited, List<Agent> sortedAgents) {
        if (!visited.contains(agent)) {
            visited.add(agent);
            for (Agent dependency : agent.getDependencies()) {
                topologicalSort(dependency, visited, sortedAgents);
            }
            sortedAgents.add(agent);
        }
    }
}
