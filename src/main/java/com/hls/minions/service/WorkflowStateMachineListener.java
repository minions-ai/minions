package com.hls.minions.service;

import com.hls.minions.model.WorkflowEvent;
import com.hls.minions.model.WorkflowState;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

@Component
public class WorkflowStateMachineListener extends StateMachineListenerAdapter<WorkflowState, WorkflowEvent> {
    @Override
    public void transition(Transition<WorkflowState, WorkflowEvent> transition) {
        super.transition(transition);
    }
}
