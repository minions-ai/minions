package com.hls.minions.service;

import com.hls.minions.model.WorkflowEvent;
import com.hls.minions.model.WorkflowState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowStateMachineInterceptor extends StateMachineInterceptorAdapter<WorkflowState, WorkflowEvent> {

    @Override
    public void postStateChange(State<WorkflowState, WorkflowEvent> state, Message<WorkflowEvent> message, Transition<WorkflowState, WorkflowEvent> transition, StateMachine<WorkflowState, WorkflowEvent> stateMachine, StateMachine<WorkflowState, WorkflowEvent> rootStateMachine) {
        super.postStateChange(state, message, transition, stateMachine, rootStateMachine);
        log.info("postStateChange stateMachine={}", stateMachine);
    }
}
