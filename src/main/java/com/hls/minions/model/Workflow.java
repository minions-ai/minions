package com.hls.minions.model;



import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

// Data model for a Workflow
@Data
@Accessors(chain = true)
public class Workflow {

    private final UUID id; // Unique identifier for the workflow
    private final String name; // Name of the workflow
    private final List<Task> tasks; // List of tasks in the workflow
    private WorkflowState state; // Current status of the workflow

    // Constructor
    public Workflow(String name, List<Task> tasks) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.tasks = tasks;
        this.state = WorkflowState.PENDING;
    }


    public void start() {
        this.state = WorkflowState.IN_PROGRESS;
        for (Task task : tasks) {
            task.setState(TaskState.IN_PROGRESS);
        }
    }

    public void complete() {
        this.state = WorkflowState.COMPLETED;
        for (Task task : tasks) {
            task.setState(TaskState.COMPLETED);
        }
    }

    public void fail() {
        this.state = WorkflowState.FAILED;
        for (Task task : tasks) {
            if (task.getState() != TaskState.COMPLETED) {
                task.setState(TaskState.FAILED);
            }
        }
    }

    @Override
    public String toString() {
        return "Workflow{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tasks=" + tasks +
                ", status=" + state +
                '}';
    }
}

