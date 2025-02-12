package com.example.multiagent.evaluation;

public class EvaluationResult {
  private String agentId;
  private double score;
  private String comments;

  public String getAgentId() {
    return agentId;
  }
  public void setAgentId(String agentId) {
    this.agentId = agentId;
  }
  public double getScore() {
    return score;
  }
  public void setScore(double score) {
    this.score = score;
  }
  public String getComments() {
    return comments;
  }
  public void setComments(String comments) {
    this.comments = comments;
  }
}
