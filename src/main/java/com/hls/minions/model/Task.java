package com.hls.minions.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// Data model for a Task

@Data
@Accessors(chain = true)
public class Task {

    private final UUID id; // Unique identifier for the task
    private String description; // Description of the task
    private String expectedOutput; // Expected output of the task
    private Agent assignedAgent; // Agent assigned to the task
    private List<Tool> tools; // Tools required for the task
    private TaskState state; // Current state of the task
    private List<Task> dependencies; // List of tasks this task depends on

    // Constructor
    public Task(String description, String expectedOutput, Agent assignedAgent, List<Tool> tools) {
        this.id = UUID.randomUUID();
        this.description = description;
        this.expectedOutput = expectedOutput;
        this.assignedAgent = assignedAgent;
        this.tools = tools;
        this.state = TaskState.PENDING; // Default state
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
    }


    // Method to check if dependencies are resolved
    public boolean areDependenciesResolved() {
        return dependencies.stream().allMatch(task -> task.getState() == TaskState.COMPLETED);
    }


    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", expectedOutput='" + expectedOutput + '\'' +
                ", assignedAgent=" + assignedAgent +
                ", tools=" + tools +
                ", state=" + state +
                '}';
    }
}