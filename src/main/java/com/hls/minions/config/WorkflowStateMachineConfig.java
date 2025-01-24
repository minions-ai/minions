package com.hls.minions.config;

import com.hls.minions.model.WorkflowEvent;
import com.hls.minions.model.WorkflowState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

// Workflow StateMachine Configuration
@Configuration
@EnableStateMachine
class WorkflowStateMachineConfig extends StateMachineConfigurerAdapter<WorkflowState, WorkflowEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<WorkflowState, WorkflowEvent> states) throws Exception {
        states
                .withStates()
                .initial(WorkflowState.PENDING)
                .state(WorkflowState.IN_PROGRESS)
                .end(WorkflowState.COMPLETED)
                .end(WorkflowState.FAILED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<WorkflowState, WorkflowEvent> transitions) throws Exception {
        transitions
                .withExternal().source(WorkflowState.PENDING).target(WorkflowState.IN_PROGRESS).event(WorkflowEvent.START)
                .and()
                .withExternal().source(WorkflowState.IN_PROGRESS).target(WorkflowState.COMPLETED).event(WorkflowEvent.TASK_COMPLETED)
                .and()
                .withExternal().source(WorkflowState.IN_PROGRESS).target(WorkflowState.FAILED).event(WorkflowEvent.TASK_FAILED);
    }
}