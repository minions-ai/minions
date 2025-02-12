package com.example.multiagent.agent;

import com.example.multiagent.manager.AgentManager;
import com.example.multiagent.task.Task;
import com.example.multiagent.tools.ToolRepository;
import com.example.multiagent.evaluation.EvaluationMetrics;
import com.example.multiagent.evaluation.EvaluationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgentCreatorAgent extends AbstractAgent {

  @Autowired
  private AgentManager agentManager;

  @Autowired
  private ToolRepository toolRepository;

  public AgentCreatorAgent() {
    super("creator-agent-1", "Agent responsible for creating new agents",
        null, new EvaluationMetrics());
  }

  @Override
  public void handleTask(Task task) {
    logger.info("AgentCreatorAgent {} handling task: {} to create new agent.", agentId, task.getTaskId());
    // Create a new agent identifier
    String newAgentId = "agent-" + System.currentTimeMillis();
    // Retrieve tools from the repository
    var tools = toolRepository.getAvailableTools();
    // Define evaluation metrics (could be enhanced)
    EvaluationMetrics metrics = new EvaluationMetrics();
    // Create a new TaskHandlerAgent instance dynamically.
    // In a production system, consider using a factory or dependency injection.
    TaskHandlerAgent newAgent = new TaskHandlerAgent();
    // Override the new agent’s id and tools as needed.
    newAgent.agentId = newAgentId;
    newAgent.availableTools = tools;
    // Register the new agent with the manager.
    agentManager.registerAgent(newAgent);
    logger.info("Created and registered new agent: {}", newAgentId);
    // Forward the task to the newly created agent.
    newAgent.handleTask(task);
  }

  @Override
  public void updateAgent(EvaluationResult result) {
    logger.info("AgentCreatorAgent {} received evaluation feedback.", agentId);
    // Optionally update creation strategies based on evaluation feedback.
  }
}
