package com.minionslab.core.agent;


import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.context.AgentContext;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the current state of an MCP agent.
 */

@Data
@Accessors(chain = true)
public class AgentState {
    private String agentId;
    private String name;
    private AgentStatus status;
    private Map<String, Object> memory;
    private List<Goal> goals;
    private AgentContext currentContext;
    private ModelConfig modelConfig;
    private Map<String, Object> metadata;

    public AgentState() {
        this.memory = new HashMap<>();
        this.goals = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.status = AgentStatus.INITIALIZED;
    }
    
    public void addGoal(Goal goal) {
        this.goals.add(goal);
    }
}