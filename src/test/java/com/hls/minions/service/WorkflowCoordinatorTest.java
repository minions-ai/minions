package com.hls.minions.service;

import com.hls.minions.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class WorkflowCoordinatorTest {

    private WorkflowCoordinator workflowCoordinator;
    private StateMachine<WorkflowState, WorkflowEvent> workflowStateMachine;
    @Autowired
    private WorkflowStateMachineInterceptor workflowStateMachineInterceptor;
    @Autowired
    private WorkflowStateMachineListener workflowStateMachineListener;


    @Test
    public void testStateMachineInPending() throws Exception {
        Workflow workflow = new DummyWorkflow();
//        workflowCoordinator.startWorkflow(workflow);
        StateMachine<WorkflowState, WorkflowEvent> machine = workflow.getStateMachine();
        StateMachineTestPlan<WorkflowState, WorkflowEvent> plan =
                StateMachineTestPlanBuilder.<WorkflowState, WorkflowEvent>builder()
                                           .defaultAwaitTime(5)
                                           .stateMachine(machine)
                                           .step()
                                           .expectState(WorkflowState.PENDING)
                                           .and()
                                           .step()
                                           .sendEvent(WorkflowEvent.START)
                                           .expectStateChanged(1)
                                           .expectState(WorkflowState.IN_PROGRESS)
                                           .and().build();
        plan.test();

    }


    @Test
    public void testStateMachineInRunning() throws Exception {
        Workflow workflow = new DummyWorkflow();
        WorkflowCoordinator coordinator = new WorkflowCoordinator(workflowStateMachineInterceptor,workflowStateMachineListener);
        coordinator.startWorkflow(workflow);
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
