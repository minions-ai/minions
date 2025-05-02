package com.minionslab.mcp.agent;

import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.message.DefaultMCPMessage;
import com.minionslab.mcp.message.MCPMessage;
import com.minionslab.mcp.message.MessageRole;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class for MCP-based agents.
 * Defines the core functionality and lifecycle management for agents.
 */
@Data
@Accessors(chain = true)
public abstract class MCPAgent {
    
    protected final String agentId = UUID.randomUUID().toString();
    
    protected AgentState state;
    protected AgentRecipe recipe;
    protected MCPMessage userMessage;
    
    public MCPAgent(AgentRecipe recipe, MCPMessage userMessage) {
        this.recipe = recipe;
        this.userMessage = userMessage;
        initialize(recipe);
    }
    
    /**
     * Initializes the agent with the given configuration.
     *
     * @param recipe The agent configuration
     */
    protected void initialize(AgentRecipe recipe) {
        this.state = new AgentState();
        
        // Initialize context with system prompt
        MCPContext context = new MCPContext(agentId, recipe);
        context.addMessage(createSystemMessage(recipe.getSystemPrompt()));
        this.state.setCurrentContext(context);
        
        // Set initial state
        this.state.setStatus(AgentStatus.INITIALIZED);
        this.state.setAgentId(generateAgentId());
        
        
        List<String> requiredTools = new ArrayList<>(recipe.getRequiredTools());
        requiredTools.addAll(getAvailableTools());
    }
    
    /**
     * Creates a system message with the given content.
     */
    protected MCPMessage createSystemMessage(String content) {
        return createMessage(content, MessageRole.SYSTEM);
    }
    
    protected MCPMessage createMessage(String content, MessageRole messageRole) {
        return DefaultMCPMessage.builder()
                                .role(messageRole)
                                .content(content)
                                .modelId(recipe.getModelConfig().getModelId())
                                .build();
    }
    
    /**
     * Generates a unique agent ID.
     */
    protected String generateAgentId() {
        return String.format("%s-%s", getClass().getSimpleName(), System.currentTimeMillis());
    }
    
    /**
     * Gets the list of tools available to this agent.
     */
    public abstract List<String> getAvailableTools();
    
    
    /**
     * Creates a user message with the given content.
     */
    protected MCPMessage createUserMessage(String content) {
        return createMessage(content, MessageRole.USER);
    }
    
    /**
     * Updates the status of the current goal based on agent state.
     */
    protected abstract void updateGoalStatus();
    
    /**
     * Performs cleanup and releases resources.
     */
    public void shutdown() {
        state.setStatus(AgentStatus.SHUTDOWN);
    }
    
    /**
     * Gets the current state of the agent.
     */
    public AgentState getState() {
        return state;
    }
    
    /**
     * Updates the agent's state.
     */
    public void setState(AgentState state) {
        this.state = state;
    }
    
    /**
     * Sets a new goal for the agent.
     */
    public void setGoal(MCPGoal goal) {
        state.getGoals().clear();
        state.addGoal(goal);
    }
    
    /**
     * Checks if the current goal has been achieved.
     */
    public boolean isGoalAchieved() {
        MCPGoal currentGoal = getCurrentGoal();
        return currentGoal != null && currentGoal.isAchieved();
    }
    
    /**
     * Gets the agent's current goal.
     */
    public MCPGoal getCurrentGoal() {
        List<MCPGoal> goals = state.getGoals();
        return goals.isEmpty() ? null : goals.get(goals.size() - 1);
    }
    
    /**
     * Gets the agent's current context.
     */
    public MCPContext getContext() {
        return state.getCurrentContext();
    }
    
    /**
     * Updates the agent's context.
     */
    public void updateContext(MCPContext context) {
        state.setCurrentContext(context);
    }
} 