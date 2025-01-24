package com.hls.minions.service;

import com.hls.minions.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkflowCoordinator {

    public void startWorkflow(Workflow workflow) {
        workflow.getStateMachine().sendEvent(WorkflowEvent.START);
        workflow.getAgents().forEach(AbstractAgent::start);
    }

    public void handleAgentCompletion(Workflow workflow) {
        boolean allAgentsCompleted = workflow.getAgents().stream()
                                             .allMatch(agent -> agent.getStateMachine().getState().getId() == AgentState.COMPLETED);

        if (allAgentsCompleted) {
            workflow.getStateMachine().sendEvent(WorkflowEvent.AGENT_COMPLETED);
        } else {
            workflow.getStateMachine().sendEvent(WorkflowEvent.AGENT_FAILED);
        }
    }
}
