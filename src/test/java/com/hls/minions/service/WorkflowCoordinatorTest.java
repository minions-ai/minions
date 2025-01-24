package com.hls.minions.service;

import static org.junit.jupiter.api.Assertions.*;

import com.hls.minions.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;

import static org.mockito.Mockito.*;

public class WorkflowCoordinatorTest {

    @Test
    public void testWorkflowCompletion() {
        // Mock state machines and agents
        StateMachine<WorkflowState, WorkflowEvent> workflowStateMachine = mock(StateMachine.class);
        StateMachine<AgentState, AgentEvent> agentStateMachine = mock(StateMachine.class);
        AbstractAgent agent = mock(AbstractAgent.class);

        when(agent.getStateMachine()).thenReturn(agentStateMachine);

        Workflow workflow = new Workflow();
        workflow.setStateMachine(workflowStateMachine);
        workflow.setAgents(List.of(agent));

        WorkflowCoordinator coordinator = new WorkflowCoordinator();
        coordinator.startWorkflow(workflow);
        coordinator.handleAgentCompletion(workflow);

        verify(workflowStateMachine).sendEvent(WorkflowEvent.AGENT_COMPLETED);
    }
}
