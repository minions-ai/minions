package com.hls.minions.model;



import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Data model for a Workflow
@Data
@Accessors(chain = true)
public class Workflow {
    private String id;
    private StateMachine<WorkflowState, WorkflowEvent> stateMachine;
    private List<AbstractAgent> agents;

    public Workflow() {
        this.stateMachine = stateMachine;
        this.id = UUID.randomUUID().toString();
        agents = new ArrayList<>();
    }

    public State<WorkflowState, WorkflowEvent> getState() {
        return this.getStateMachine().getState();
    }

    public void addAgent(AbstractAgent agent) {
        agents.add(agent);
    }
}

