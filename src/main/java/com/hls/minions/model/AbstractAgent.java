package com.hls.minions.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.statemachine.StateMachine;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public abstract class AbstractAgent {
    private UUID id;
    private StateMachine<AgentState, AgentEvent> stateMachine;
    private List<Task> tasks;

    public void start() {
        stateMachine.sendEvent(AgentEvent.START);
    }

    public void complete() {
        stateMachine.sendEvent(AgentEvent.COMPLETE);
    }

    public void fail() {
        stateMachine.sendEvent(AgentEvent.FAIL);
    }

    // Hooks for custom logic
    protected abstract void beforeStart();
    protected abstract void afterComplete();
    protected abstract void afterFail();
}
