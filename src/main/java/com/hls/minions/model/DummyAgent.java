package com.hls.minions.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.ArrayList;
import java.util.EnumSet;

@Slf4j
public class DummyAgent extends Agent {
    public DummyAgent() throws Exception {
        super("Dummy Agent", null, null, null, new ArrayList<>());
        this.setStateMachine(buildStateMachine());
        this.getStateMachine().addStateListener(getStateListener());
    }

    private StateMachineListener<AgentState, AgentEvent> getStateListener() {
        return new StateMachineListenerAdapter<AgentState, AgentEvent>() {
            @Override
            public void stateChanged(State<AgentState, AgentEvent> from, State<AgentState, AgentEvent> to) {
                super.stateChanged(from, to);
                AgentState state = to.getId();
                switch (state){
                    case WORKING -> executeTask();
                    case COMPLETED -> executeCompletion();
                    case FAILED -> executeFail();
                }

            }
        };

    }

    private void executeFail() {

    }

    private void executeCompletion() {

    }

    private void executeTask() {
        log.info("Task executed for agent {}", this.getName());
        this.getStateMachine().sendEvent(AgentEvent.COMPLETE);
    }

    private StateMachine<AgentState, AgentEvent> buildStateMachine() throws Exception {
        StateMachineBuilder.Builder<AgentState, AgentEvent> builder = StateMachineBuilder.builder();
        builder.configureStates()
               .withStates().initial(AgentState.IDLE)
               .states(EnumSet.allOf(AgentState.class));
        builder.configureTransitions().withExternal()
               .source(AgentState.IDLE).target(AgentState.WORKING).event(AgentEvent.START)
               .and()
               .withExternal()
               .source(AgentState.WORKING).target(AgentState.COMPLETED).event(AgentEvent.COMPLETE)
               .and()
               .withExternal()
               .source(AgentState.WORKING).target(AgentState.ERROR).event(AgentEvent.FAIL);
        return builder.build();
    }
}
