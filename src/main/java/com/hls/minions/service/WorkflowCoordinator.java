package com.hls.minions.service;

import com.hls.minions.model.*;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// WorkflowCoordinator with StateMachine Integration
class WorkflowCoordinator {

    private final List<Workflow> workflows; // List of all workflows
    private final StateMachine<WorkflowState, WorkflowEvent> workflowStateMachine;

    public WorkflowCoordinator(StateMachine<WorkflowState, WorkflowEvent> workflowStateMachine) {
        this.workflows = new ArrayList<>();
        this.workflowStateMachine = workflowStateMachine;
        this.workflowStateMachine.addStateListener(new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<WorkflowState, WorkflowEvent> from, State<WorkflowState, WorkflowEvent> to) {
                System.out.println("Workflow state changed from " + from.getId() + " to " + to.getId());
            }

            @Override
            public void transitionEnded(Transition<WorkflowState, WorkflowEvent> transition) {
                System.out.println("Transition ended: " + transition);
            }
        });
    }

    public void startWorkflow(UUID workflowId) {
        Workflow workflow = workflows.stream()
                .filter(w -> w.getId().equals(workflowId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));

        workflowStateMachine.sendEvent(WorkflowEvent.START);
        workflow.setState(WorkflowState.IN_PROGRESS);
        workflow.getTasks().forEach(this::executeTask);
    }

    private void executeTask(Task task) {
        StateMachine<TaskState, TaskEvent> taskStateMachine = TaskStateMachineFactory.createStateMachine();
        taskStateMachine.start();

        taskStateMachine.sendEvent(TaskEvent.START);
        System.out.println("Executing task: " + task.getDescription());

        try {
            // Simulate tool execution
            for (Tool tool : task.getTools()) {
                tool.getExecutor().execute();
            }
            taskStateMachine.sendEvent(TaskEvent.COMPLETE);
            System.out.println("Task completed: " + task.getDescription());

            workflowStateMachine.sendEvent(WorkflowEvent.TASK_COMPLETED);
        } catch (Exception e) {
            taskStateMachine.sendEvent(TaskEvent.FAIL);
            System.err.println("Task failed: " + task.getDescription());

            workflowStateMachine.sendEvent(WorkflowEvent.TASK_FAILED);
        }
    }

    public void addWorkflow(Workflow workflow) {
        workflows.add(workflow);
    }
}