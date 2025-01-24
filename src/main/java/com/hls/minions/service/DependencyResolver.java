package com.hls.minions.service;

import com.hls.minions.model.Task;
import com.hls.minions.model.TaskState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// DependencyResolver class for managing task dependencies
public class DependencyResolver {

    // Validate that the dependency graph is acyclic
    public boolean isDependencyGraphValid(List<Task> tasks) {
        try {
            List<Task> visited = new ArrayList<>();
            for (Task task : tasks) {
                checkForCycles(task, visited, new ArrayList<>());
            }
            return true;
        } catch (IllegalStateException e) {
            System.err.println("Invalid dependency graph: " + e.getMessage());
            return false;
        }
    }

    // Recursive method to check for cycles
    private void checkForCycles(Task task, List<Task> visited, List<Task> stack) {
        if (stack.contains(task)) {
            throw new IllegalStateException("Circular dependency detected for task: " + task.getDescription());
        }
        if (!visited.contains(task)) {
            stack.add(task);
            for (Task dependency : task.getDependencies()) {
                checkForCycles(dependency, visited, stack);
            }
            stack.remove(task);
            visited.add(task);
        }
    }

    // Get tasks that are ready for execution
    public List<Task> getExecutableTasks(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getState() == TaskState.PENDING && task.areDependenciesResolved())
                .collect(Collectors.toList());
    }

    // Mark a task as completed and update dependents
    public void markTaskCompleted(Task completedTask, List<Task> tasks) {
        tasks.stream()
                .filter(task -> task.getDependencies().contains(completedTask))
                .forEach(task -> {
                    if (task.areDependenciesResolved()) {
                        System.out.println("Task ready for execution: " + task.getDescription());
                    }
                });
    }

    // Determine execution order using topological sort
    public List<Task> determineExecutionOrder(List<Task> tasks) {
        List<Task> sortedTasks = new ArrayList<>();
        List<Task> visited = new ArrayList<>();

        for (Task task : tasks) {
            topologicalSort(task, visited, sortedTasks);
        }

        return sortedTasks;
    }

    // Recursive topological sort
    private void topologicalSort(Task task, List<Task> visited, List<Task> sortedTasks) {
        if (!visited.contains(task)) {
            visited.add(task);
            for (Task dependency : task.getDependencies()) {
                topologicalSort(dependency, visited, sortedTasks);
            }
            sortedTasks.add(task);
        }
    }
}
