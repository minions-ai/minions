// File: agent/TaskHandlerAgent.java
package com.example.multiagent.agent;

import com.example.multiagent.manager.AgentManager;
import com.example.multiagent.task.Task;
import com.example.multiagent.evaluation.EvaluationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TaskHandlerAgent extends AbstractAgent {

  @Autowired
  private AgentManager agentManager;

  public TaskHandlerAgent() {
    // In a production system, these values could be injected from configuration
    super("handler-agent-1", "Agent for task decomposition and delegation",
        null,  // Tools will be assigned as needed from ToolRepository
        new com.example.multiagent.evaluation.EvaluationMetrics());
  }

  @Override
  @Async("agentExecutor")
  public void handleTask(Task task) {
    logger.info("TaskHandlerAgent {} handling task: {}", agentId, task.getTaskId());
    // 1. Decompose the task if necessary. (For simplicity, assume task has subTasks if already decomposed)
    if(task.getSubTasks() != null && !task.getSubTasks().isEmpty()){
      logger.info("Decomposed task into {} subtasks.", task.getSubTasks().size());
      task.getSubTasks().forEach(subTask -> {
        // Dispatch each subtask using AgentManager
        agentManager.dispatchTask(subTask);
      });
    } else {
      // If no decomposition is needed, process the task directly.
      logger.info("Processing atomic task: {}", task.getTaskId());
      // [Here you would call tools or do the work]
    }
    // After execution, you would typically aggregate results and notify the requestor.
    logger.info("Task {} processing complete.", task.getTaskId());
  }

  @Override
  public void updateAgent(EvaluationResult result) {
    logger.info("TaskHandlerAgent {} updating based on evaluation: {}", agentId, result.getScore());
    // Update internal strategies, thresholds, etc.
  }
}
