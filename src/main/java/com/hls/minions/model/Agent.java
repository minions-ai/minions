package com.hls.minions.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

// Data model for an Agent
@Data
@Accessors(chain = true)
public class Agent extends AbstractAgent{

    private final UUID id; // Unique identifier for each agent
    private String name; // Name of the agent
    private String role; // Role of the agent (e.g., DataExtractor, WorkflowManager)
    private String goal; // Goal of the agent (e.g., "Extract data from source X")
    private String backstory; // Background or purpose of the agent
    private List<Tool> tools; // List of tools available to the agent
    private AgentState state; // Current state of the task
    private List<Agent> dependencies; // List of tasks this task depends on

    // Constructor
    public Agent(String name, String role, String goal, String backstory, List<Tool> tools) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.role = role;
        this.goal = goal;
        this.backstory = backstory;
        this.tools = tools;
    }

    // Method to check if dependencies are resolved
    public boolean areDependenciesResolved() {
        return dependencies.stream().allMatch(task -> task.getState() == AgentState.COMPLETED);
    }

    @Override
    protected void beforeStart() {

    }

    @Override
    protected void afterComplete() {

    }

    @Override
    protected void afterFail() {

    }
}

