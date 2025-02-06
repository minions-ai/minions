package com.hls.minions.config;

import com.hls.minions.model.AgentEvent;
import com.hls.minions.model.AgentState;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@EnableStateMachine
public class AgentStateMachineConfig extends StateMachineConfigurerAdapter<AgentState, AgentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<AgentState, AgentEvent> states) throws Exception {
        states.withStates()
              .initial(AgentState.IDLE)
              .states(EnumSet.allOf(AgentState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<AgentState, AgentEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(AgentState.IDLE).target(AgentState.WORKING).event(AgentEvent.START)
                .and()
                .withExternal()
                .source(AgentState.WORKING).target(AgentState.COMPLETED).event(AgentEvent.COMPLETE)
                .and()
                .withExternal()
                .source(AgentState.WORKING).target(AgentState.ERROR).event(AgentEvent.FAIL);
    }
}