package com.hls.minions.service;

import com.hls.minions.model.TaskEvent;
import com.hls.minions.model.TaskState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;

public class TaskStateMachineFactory {

    public static StateMachine<TaskState, TaskEvent> createStateMachine() {
        try {
            StateMachineBuilder.Builder<TaskState, TaskEvent> builder = StateMachineBuilder.builder();

            // Define states
            builder.configureStates()
                    .withStates()
                    .initial(TaskState.PENDING)
                    .state(TaskState.IN_PROGRESS)
                    .end(TaskState.COMPLETED)
                    .end(TaskState.FAILED);

            // Define transitions
            builder.configureTransitions()
                    .withExternal().source(TaskState.PENDING).target(TaskState.IN_PROGRESS).event(TaskEvent.START)
                    .and()
                    .withExternal().source(TaskState.IN_PROGRESS).target(TaskState.COMPLETED).event(TaskEvent.COMPLETE)
                    .and()
                    .withExternal().source(TaskState.IN_PROGRESS).target(TaskState.FAILED).event(TaskEvent.FAIL);

            return builder.build();
        } catch (Exception e) {
            throw new IllegalStateException("Error creating TaskStateMachine", e);
        }
    }
}

