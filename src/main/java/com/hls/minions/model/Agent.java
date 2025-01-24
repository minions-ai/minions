package com.hls.minions.model;

import java.util.List;
import java.util.UUID;

// Data model for an Agent
public class Agent {

    private final UUID id; // Unique identifier for each agent
    private String name; // Name of the agent
    private String role; // Role of the agent (e.g., DataExtractor, WorkflowManager)
    private String goal; // Goal of the agent (e.g., "Extract data from source X")
    private String backstory; // Background or purpose of the agent
    private List<Tool> tools; // List of tools available to the agent

    // Constructor
    public Agent(String name, String role, String goal, String backstory, List<Tool> tools) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.role = role;
        this.goal = goal;
        this.backstory = backstory;
        this.tools = tools;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getBackstory() {
        return backstory;
    }

    public void setBackstory(String backstory) {
        this.backstory = backstory;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public void setTools(List<Tool> tools) {
        this.tools = tools;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", goal='" + goal + '\'' +
                ", backstory='" + backstory + '\'' +
                ", tools=" + tools +
                '}';
    }
}

