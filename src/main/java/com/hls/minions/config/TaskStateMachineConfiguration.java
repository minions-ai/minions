package com.hls.minions.config;

import com.hls.minions.model.TaskEvent;
import com.hls.minions.model.TaskState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

// Task StateMachine Configuration
@Configuration
@EnableStateMachine
class TaskStateMachineConfig extends StateMachineConfigurerAdapter<TaskState, TaskEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<TaskState, TaskEvent> states) throws Exception {
        states
                .withStates()
                .initial(TaskState.PENDING)
                .state(TaskState.IN_PROGRESS)
                .end(TaskState.COMPLETED)
                .end(TaskState.FAILED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<TaskState, TaskEvent> transitions) throws Exception {
        transitions
                .withExternal().source(TaskState.PENDING).target(TaskState.IN_PROGRESS).event(TaskEvent.START)
                .and()
                .withExternal().source(TaskState.IN_PROGRESS).target(TaskState.COMPLETED).event(TaskEvent.COMPLETE)
                .and()
                .withExternal().source(TaskState.IN_PROGRESS).target(TaskState.FAILED).event(TaskEvent.FAIL);
    }
}
