package com.hls.minions.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
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


    // Constructor
    public Task(String description, String expectedOutput, Agent assignedAgent, List<Tool> tools) {
        this.id = UUID.randomUUID();
        this.description = description;
        this.expectedOutput = expectedOutput;
        this.assignedAgent = assignedAgent;
        this.tools = tools;
    }




    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", expectedOutput='" + expectedOutput + '\'' +
                ", assignedAgent=" + assignedAgent +
                ", tools=" + tools +
                '}';
    }
}