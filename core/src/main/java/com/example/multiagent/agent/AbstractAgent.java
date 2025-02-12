// File: agent/AbstractAgent.java
package com.example.multiagent.agent;

import com.example.multiagent.task.Task;
import com.example.multiagent.tools.Tool;
import com.example.multiagent.evaluation.EvaluationMetrics;
import com.example.multiagent.evaluation.EvaluationResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAgent implements Agent {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected String agentId;
  protected String description;
  protected List<Tool> availableTools;
  protected EvaluationMetrics evaluationMetrics;

  public AbstractAgent(String agentId, String description, List<Tool> availableTools, EvaluationMetrics evaluationMetrics) {
    this.agentId = agentId;
    this.description = description;
    this.availableTools = availableTools;
    this.evaluationMetrics = evaluationMetrics;
  }

  @Override
  public String getAgentId() {
    return agentId;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public List<Tool> getAvailableTools() {
    return availableTools;
  }

  @Override
  public EvaluationMetrics getEvaluationMetrics() {
    return evaluationMetrics;
  }

  @Override
  public abstract void handleTask(Task task);

  @Override
  public abstract void updateAgent(EvaluationResult result);
}
