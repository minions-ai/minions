package com.hls.minions.service;

import com.hls.minions.model.AgentState;
import com.hls.minions.model.Workflow;
import com.hls.minions.model.WorkflowEvent;
import com.hls.minions.model.WorkflowState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
@Slf4j
public class WorkflowCoordinator {


    private final WorkflowStateMachineInterceptor workflowStateMachineInterceptor;
    private final WorkflowStateMachineListener workflowStateMachineListener;

    public WorkflowCoordinator(WorkflowStateMachineInterceptor workflowStateMachineInterceptor, WorkflowStateMachineListener workflowStateMachineListener) {

        this.workflowStateMachineInterceptor = workflowStateMachineInterceptor;
        this.workflowStateMachineListener = workflowStateMachineListener;
    }

    public void startWorkflow(Workflow workflow) throws Exception {
        workflow.setStateMachine(buildStateMachine());
        workflow.getStateMachine().startReactively();
        workflow.getStateMachine().sendEvent(WorkflowEvent.START);

//        workflow.getAgents().forEach(AbstractAgent::start);
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


    private StateMachine<WorkflowState, WorkflowEvent> buildStateMachine() throws Exception {
        StateMachineBuilder.Builder<WorkflowState, WorkflowEvent> builder = StateMachineBuilder.builder();
        builder.configureStates().withStates()
               .initial(WorkflowState.PENDING)
               .states(EnumSet.allOf(WorkflowState.class))
               .end(WorkflowState.COMPLETED)
               .end(WorkflowState.FAILED);
        builder.configureTransitions().withExternal()
               .source(WorkflowState.PENDING).target(WorkflowState.IN_PROGRESS).event(WorkflowEvent.START)
               .and()
               .withExternal()
               .source(WorkflowState.IN_PROGRESS).target(WorkflowState.COMPLETED).event(WorkflowEvent.AGENT_COMPLETED)
               .and()
               .withExternal()
               .source(WorkflowState.IN_PROGRESS).target(WorkflowState.FAILED).event(WorkflowEvent.AGENT_FAILED);

        StateMachine<WorkflowState, WorkflowEvent> stateMachine = builder.build();
        stateMachine.addStateListener(workflowStateMachineListener);
        stateMachine.getStateMachineAccessor().doWithAllRegions(sma -> sma.addStateMachineInterceptor(workflowStateMachineInterceptor));
        return stateMachine;
    }
}
