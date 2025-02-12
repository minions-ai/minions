package com.example.multiagent.agent;

import com.example.multiagent.evaluation.EvaluationMetrics;
import com.example.multiagent.evaluation.EvaluationResult;
import com.example.multiagent.task.Task;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EvaluationAgent extends AbstractAgent {

  public EvaluationAgent() {
    super("evaluation-agent-1", "Agent responsible for evaluating task results",
        null, new EvaluationMetrics());
  }

  @Override
  @Async("agentExecutor")
  public void handleTask(Task task) {
    // In our design, evaluation may be invoked after task completion.
    logger.info("EvaluationAgent {} evaluating task: {}", agentId, task.getTaskId());
    // The actual evaluation logic can be triggered here.
  }

  @Override
  public void updateAgent(EvaluationResult result) {
    logger.info("EvaluationAgent {} does not update based on its own evaluation.", agentId);
    // Typically, evaluation agents are static in behavior.
  }

  // Evaluate a completed task and return an evaluation result.
  public EvaluationResult evaluate(Task task, Object aggregatedResult) {
    EvaluationResult result = new EvaluationResult();
    result.setAgentId(task.getTaskId());
    // Example evaluation parameters; in a real scenario, these would be computed.
    result.setScore(0.95);
    result.setComments("High accuracy and performance.");
    logger.info("Evaluation complete for task {}: score {}", task.getTaskId(), result.getScore());
    return result;
  }
}
