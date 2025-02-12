package com.example.multiagent.agent;

import com.example.multiagent.evaluation.EvaluationMetrics;
import com.example.multiagent.evaluation.EvaluationResult;
import com.example.multiagent.task.Task;
import com.example.multiagent.tools.Tool;
import java.util.List;

public interface Agent {

  String getAgentId();

  String getDescription();

  List<Tool> getAvailableTools();

  EvaluationMetrics getEvaluationMetrics();

  // Processes a task or subtask
  void handleTask(Task task);

  // Updates the agent based on evaluation results
  void updateAgent(EvaluationResult result);
}
