package com.minionslab.core.agent;

import com.minionslab.core.common.logging.LoggingTopics;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.model.MessageBundle;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract base class for MCP-based agents. Defines the core functionality, lifecycle,
 * and extensibility points for agent orchestration, state management, and tool integration.
 * <p>
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Override {@link #initialize(AgentRecipe)} to customize agent initialization, state, or tool registration.</li>
 *   <li>Override {@link #getAvailableTools()} to provide custom tool selection or registration logic.</li>
 *   <li>Override state management methods to add custom fields, metadata, or orchestration logic.</li>
 *   <li>Add new fields or methods for custom agent metadata, orchestration, or behaviors.</li>
 * </ul>
 * <b>Usage:</b> To create a new agent type, extend this class and implement required methods. Agents are designed to be highly extensible and pluggable, supporting custom goals, memory, and execution flows.
 */
@Data
@Accessors(chain = true)
@Slf4j(topic = LoggingTopics.AGENT)
public abstract class Agent {
    /**
     * Unique agent ID for traceability and context management.
     */
    protected final String agentId = UUID.randomUUID().toString();
    /**
     * The current state of the agent (goals, context, status, etc.).
     */
    protected AgentState state;
    /**
     * The agent's configuration/recipe.
     */
    protected AgentRecipe recipe;
    /**
     * The initial user message for this agent session.
     */
    protected Message userMessage;
    /**
     * The bundle of messages (history, context, etc.) for this agent.
     */
    protected MessageBundle messageBundle;

    /**
     * Constructs an agent with the given recipe and user message.
     *
     * @param recipe the agent configuration
     * @param userMessage the initial user message
     */
    public Agent(AgentRecipe recipe, Message userMessage) {
        this.recipe = recipe;
        this.messageBundle = new MessageBundle(recipe.getMessageBundle());
        this.messageBundle.addMessage(userMessage);
        this.userMessage = userMessage;
        initialize(recipe);
    }

    /**
     * Initializes the agent with the given configuration. Subclasses can override to
     * customize state, tool registration, or other setup logic.
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
     * Generates a unique agent ID. Subclasses can override for custom ID schemes.
     */
    protected String generateAgentId() {
        return String.format("%s-%s", getClass().getSimpleName(), System.currentTimeMillis());
    }

    /**
     * Gets the list of tools available to this agent. Subclasses should override to
     * provide custom tool selection or registration logic.
     *
     * @return the list of available tool names
     */
    public abstract List<String> getAvailableTools();

    /**
     * Creates a system message with the given content.
     *
     * @param content the message content
     * @return a system message
     */
    protected Message createSystemMessage(String content) {
        return createMessage(content, MessageRole.SYSTEM);
    }

    /**
     * Creates a message with the given content and role.
     *
     * @param content the message content
     * @param messageRole the message role
     * @return a message
     */
    protected Message createMessage(String content, MessageRole messageRole) {
        return new DefaultMessage(MessageScope.AGENT, messageRole, content, Map.of());
    }

    /**
     * Performs cleanup and releases resources. Subclasses can override for custom shutdown logic.
     */
    public void shutdown() {
        state.setStatus(AgentStatus.SHUTDOWN);
    }

    /**
     * Gets the current state of the agent.
     *
     * @return the agent state
     */
    public AgentState getState() {
        return state;
    }

    /**
     * Updates the agent's state.
     *
     * @param state the new agent state
     */
    public void setState(AgentState state) {
        this.state = state;
    }

    /**
     * Checks if the current goal has been achieved.
     *
     * @return true if the goal is achieved
     */
    public boolean isGoalAchieved() {
        Goal currentGoal = getCurrentGoal();
        return currentGoal != null && currentGoal.isAchieved();
    }

    /**
     * Gets the agent's current goal.
     *
     * @return the current goal, or null if none
     */
    public Goal getCurrentGoal() {
        List<Goal> goals = state.getGoals();
        return goals.isEmpty() ? null : goals.get(goals.size() - 1);
    }

    /**
     * Gets the agent's current context.
     *
     * @return the current agent context
     */
    public AgentContext getContext() {
        return state.getCurrentContext();
    }

    /**
     * Updates the agent's context.
     *
     * @param context the new agent context
     */
    public void updateContext(AgentContext context) {
        state.setCurrentContext(context);
    }

    /**
     * Gets the message bundle for this agent.
     *
     * @return the message bundle
     */
    public MessageBundle getMessageBundle() {
        return messageBundle;
    }

    /**
     * Sets the message bundle for this agent.
     *
     * @param messageBundle the new message bundle
     */
    public void setMessageBundle(MessageBundle messageBundle) {
        this.messageBundle = messageBundle;
    }
}