package com.example.multiagent.manager;

import com.example.multiagent.agent.Agent;
import com.example.multiagent.task.Task;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentManager {

  // Thread-safe registry for agents
  private Map<String, Agent> agentRegistry = new ConcurrentHashMap<>();

  public void registerAgent(Agent agent) {
    agentRegistry.put(agent.getAgentId(), agent);
    System.out.println("Agent registered: " + agent.getAgentId());
  }

  public Agent findSuitableAgent(Task task) {
    // Matching logic: for now, we return the first available agent that is not the creator.
    return agentRegistry.values().stream()
        .filter(a -> !a.getAgentId().equals("creator-agent-1"))
        .findFirst().orElse(null);
  }

  public void dispatchTask(Task task) {
    Agent agent = findSuitableAgent(task);
    if (agent != null) {
      agent.handleTask(task);
    } else {
      // No suitable agent found; delegate to the AgentCreatorAgent.
      Agent creator = agentRegistry.get("creator-agent-1");
      if (creator != null) {
        creator.handleTask(task);
      }
    }
  }
}
