package com.minionslab.core.agent;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.model.MessageBundle;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract base class for MCP-based agents.
 * Defines the core functionality and lifecycle management for agents.
 */
@Data
@Accessors(chain = true)
public abstract class Agent {
    
    protected final String agentId = UUID.randomUUID().toString();
    
    protected AgentState state;
    protected AgentRecipe recipe;
    protected Message userMessage;
    protected MessageBundle messageBundle;
    
    public Agent(AgentRecipe recipe, Message userMessage) {
        this.recipe = recipe;
        this.messageBundle = new MessageBundle(recipe.getMessageBundle());
        this.messageBundle.addMessage(userMessage);
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
        
        
        // Set initial state
        this.state.setStatus(AgentStatus.INITIALIZED);
        this.state.setAgentId(generateAgentId());
        
        
        List<String> requiredTools = new ArrayList<>(recipe.getRequiredTools());
        requiredTools.addAll(getAvailableTools());
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
     * Creates a system message with the given content.
     */
    protected Message createSystemMessage(String content) {
        return createMessage(content, MessageRole.SYSTEM);
    }
    
    protected Message createMessage(String content, MessageRole messageRole) {
        return new DefaultMessage(MessageScope.AGENT, messageRole, content, Map.of());
    }
    
    
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
     * Checks if the current goal has been achieved.
     */
    public boolean isGoalAchieved() {
        Goal currentGoal = getCurrentGoal();
        return currentGoal != null && currentGoal.isAchieved();
    }
    
    /**
     * Gets the agent's current goal.
     */
    public Goal getCurrentGoal() {
        List<Goal> goals = state.getGoals();
        return goals.isEmpty() ? null : goals.get(goals.size() - 1);
    }
    
    /**
     * Gets the agent's current context.
     */
    public AgentContext getContext() {
        return state.getCurrentContext();
    }
    
    /**
     * Updates the agent's context.
     */
    public void updateContext(AgentContext context) {
        state.setCurrentContext(context);
    }
    
    public MessageBundle getMessageBundle() {
        return messageBundle;
    }
    
    public void setMessageBundle(MessageBundle messageBundle) {
        this.messageBundle = messageBundle;
    }
}