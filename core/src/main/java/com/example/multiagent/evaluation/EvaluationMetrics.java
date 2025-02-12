// File: evaluation/EvaluationMetrics.java
package com.example.multiagent.evaluation;

public class EvaluationMetrics {
  // For example, we include accuracy and responseTime.
  private double accuracy;
  private long responseTime; // in milliseconds

  public double getAccuracy() {
    return accuracy;
  }
  public void setAccuracy(double accuracy) {
    this.accuracy = accuracy;
  }
  public long getResponseTime() {
    return responseTime;
  }
  public void setResponseTime(long responseTime) {
    this.responseTime = responseTime;
  }
}
