package com.example.multiagent.task;

import java.util.List;
import java.util.UUID;

public class Task {
  private String taskId;
  private String description;
  private List<Task> subTasks;  // For hierarchical decomposition

  public Task(String description) {
    this.taskId = UUID.randomUUID().toString();
    this.description = description;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getDescription() {
    return description;
  }

  public List<Task> getSubTasks() {
    return subTasks;
  }

  public void setSubTasks(List<Task> subTasks) {
    this.subTasks = subTasks;
  }
}
