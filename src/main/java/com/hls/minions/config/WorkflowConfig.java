package com.hls.minions.config;

import java.util.List;

public class WorkflowConfig {
    private String name;
    private List<String> tasks;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }
}

