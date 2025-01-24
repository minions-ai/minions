package com.hls.minions.model;

// Data model for a Tool
public class Tool {

    private final String name; // Name of the tool
    private final String description; // Description of the tool's functionality
    private final ToolExecutor executor; // Logic or integration handler for the tool

    // Constructor
    public Tool(String name, String description, ToolExecutor executor) {
        this.name = name;
        this.description = description;
        this.executor = executor;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ToolExecutor getExecutor() {
        return executor;
    }

    @Override
    public String toString() {
        return "Tool{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
