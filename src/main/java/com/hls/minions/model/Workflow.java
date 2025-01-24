package com.hls.minions.model;



import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.statemachine.StateMachine;

import java.util.List;
import java.util.UUID;

// Data model for a Workflow
@Data
@Accessors(chain = true)
public class Workflow {
    private String id;
    private StateMachine<WorkflowState, WorkflowEvent> stateMachine;
    private List<AbstractAgent> agents;
}

