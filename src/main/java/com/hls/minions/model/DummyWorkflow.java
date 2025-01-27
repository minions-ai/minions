package com.hls.minions.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@Slf4j
public class DummyWorkflow extends Workflow {

    public DummyWorkflow() throws Exception {
        this.addAgent(new DummyAgent());
        StateMachine<WorkflowState, WorkflowEvent> stateMachine = buildStateMachine();
        this.setStateMachine(stateMachine);
        this.getStateMachine().addStateListener(getWorkflowStateListener());

    }

    private StateMachineListener<WorkflowState, WorkflowEvent> getWorkflowStateListener() {
        return new StateMachineListenerAdapter<WorkflowState, WorkflowEvent>() {
            @Override
            public void stateChanged(State<WorkflowState, WorkflowEvent> from, State<WorkflowState, WorkflowEvent> to) {
                super.stateChanged(from, to);
                if (to.getId().equals(WorkflowState.IN_PROGRESS)) {
                    getAgents().forEach(agent -> {
                        agent.getStateMachine().startReactively();
                        agent.getStateMachine().sendEvent(AgentEvent.START);
                    });
                }
            }

        };
    }

    private StateMachine<WorkflowState, WorkflowEvent> buildStateMachine() throws Exception {
        StateMachineBuilder.Builder<WorkflowState, WorkflowEvent> builder = StateMachineBuilder.builder();
        builder.configureStates().withStates()
               .initial(WorkflowState.PENDING, getInitiAction())
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
        return stateMachine;
    }

    private Action<WorkflowState, WorkflowEvent> getInitiAction() {
        Action<WorkflowState, WorkflowEvent> action = new Action<WorkflowState, WorkflowEvent>() {
            @Override
            public void execute(StateContext<WorkflowState, WorkflowEvent> context) {
                getAgents().forEach(agent -> {
                    agent.getStateMachine().addStateListener(buildAgentStateListener());
//                    agent.getStateMachine().sendEvent(AgentEvent.START);
                });
            }
        };
        return action;
    }

    private StateMachineListener<AgentState, AgentEvent> buildAgentStateListener() {
        StateMachineListener<AgentState, AgentEvent> listener = new StateMachineListenerAdapter<AgentState, AgentEvent>() {
            @Override
            public void stateChanged(State<AgentState, AgentEvent> from, State<AgentState, AgentEvent> to) {
                super.stateChanged(from, to);
                log.info("stateChanged from {} to {}", from, to);
                switch (to.getId()) {
                    case AgentState.WORKING -> log.info("Agent started");
                    case AgentState.COMPLETED -> getStateMachine().sendEvent(WorkflowEvent.AGENT_COMPLETED);
                    case AgentState.FAILED -> getStateMachine().sendEvent(WorkflowEvent.AGENT_FAILED);
                }

            }
        };
        return listener;
    }
}
