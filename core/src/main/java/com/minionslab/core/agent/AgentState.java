package com.minionslab.core.agent;


import com.minionslab.core.config.ModelConfig;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AgentState represents the current state of an MCP agent, including its ID, name, status,
 * memory, goals, context, model configuration, and metadata.
 * <p>
 * This class is designed for extensibility: you can add fields for custom state, tracking,
 * or advanced goal management. It is the primary carrier for agent state during execution
 * and orchestration.
 */
@Data
@Accessors(chain = true)
public class AgentState {
    /**
     * The unique agent ID for this state.
     */
    private String agentId;
    /**
     * The agent's name.
     */
    private String name;
    /**
     * The current status of the agent.
     */
    private AgentStatus status;
    /**
     * Arbitrary memory or state for extensibility.
     */
    private Map<String, Object> memory;
    /**
     * The list of goals for this agent.
     */
    private List<Goal> goals;
    /**
     * The current context for this agent.
     */
    private AgentContext currentContext;
    /**
     * The model configuration for this agent.
     */
    private ModelConfig modelConfig;
    /**
     * Arbitrary metadata for extensibility and custom state.
     */
    private Map<String, Object> metadata;

    /**
     * Constructs a new AgentState with default values.
     */
    public AgentState() {
        this.memory = new HashMap<>();
        this.goals = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.status = AgentStatus.INITIALIZED;
    }
    
    /**
     * Add a goal to this agent's state.
     *
     * @param goal the goal to add
     */
    public void addGoal(Goal goal) {
        this.goals.add(goal);
    }
}