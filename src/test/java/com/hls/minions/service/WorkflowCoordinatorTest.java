package com.hls.minions.service;

import static org.junit.jupiter.api.Assertions.*;

import com.hls.minions.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class WorkflowCoordinatorTest {

    private WorkflowCoordinator workflowCoordinator;
    private StateMachine<WorkflowState, WorkflowEvent> workflowStateMachine;

    @BeforeEach
    public void setUp() throws Exception {
        // Create a StateMachine for testing
        StateMachineBuilder.Builder<WorkflowState, WorkflowEvent> builder = StateMachineBuilder.builder();
        builder.configureStates()
                .withStates()
                .initial(WorkflowState.PENDING)
                .state(WorkflowState.IN_PROGRESS)
                .end(WorkflowState.COMPLETED)
                .end(WorkflowState.FAILED);

        builder.configureTransitions()
                .withExternal().source(WorkflowState.PENDING).target(WorkflowState.IN_PROGRESS).event(WorkflowEvent.START)
                .and()
                .withExternal().source(WorkflowState.IN_PROGRESS).target(WorkflowState.COMPLETED).event(WorkflowEvent.TASK_COMPLETED)
                .and()
                .withExternal().source(WorkflowState.IN_PROGRESS).target(WorkflowState.FAILED).event(WorkflowEvent.TASK_FAILED);

        workflowStateMachine = builder.build();
        workflowStateMachine.start();

        workflowCoordinator = new WorkflowCoordinator(workflowStateMachine);
    }

    @Test
    public void testStartWorkflow() {
        Workflow workflow = new Workflow("Test Workflow", createTestTasks());
        workflowCoordinator.addWorkflow(workflow);

        workflowCoordinator.startWorkflow(workflow.getId());

        assertEquals(WorkflowState.IN_PROGRESS, workflow.getState(), "Workflow should be in progress after start");
    }

    @Test
    public void testCompleteWorkflow() {
        Workflow workflow = new Workflow("Test Workflow", createTestTasks());
        workflowCoordinator.addWorkflow(workflow);

        workflowCoordinator.startWorkflow(workflow.getId());

        // Simulate all tasks being completed
        workflow.getTasks().forEach(task -> task.setState(TaskState.COMPLETED));
        workflowStateMachine.sendEvent(WorkflowEvent.TASK_COMPLETED);

        assertEquals(WorkflowState.COMPLETED, workflow.getState(), "Workflow should be completed after all tasks are done");
    }

    @Test
    public void testFailWorkflow() {
        Workflow workflow = new Workflow("Test Workflow", createTestTasks());
        workflowCoordinator.addWorkflow(workflow);

        workflowCoordinator.startWorkflow(workflow.getId());

        // Simulate a task failing
        workflow.getTasks().get(0).setState(TaskState.FAILED);
        workflowStateMachine.sendEvent(WorkflowEvent.TASK_FAILED);

        assertEquals(WorkflowState.FAILED, workflow.getState(), "Workflow should fail if a task fails");
    }

    private List<Task> createTestTasks() {
        List<Task> tasks = new ArrayList<>();
        Task task1 = new Task("Task 1", "Output 1", null, new ArrayList<>());
        Task task2 = new Task("Task 2", "Output 2", null, new ArrayList<>());
        tasks.add(task1);
        tasks.add(task2);
        return tasks;
    }
}
