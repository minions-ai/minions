package com.minionslab.mcp.agent;

import com.minionslab.mcp.message.MCPMessage;

import java.util.Collections;
import java.util.List;

/**
 * Default concrete implementation of MCPAgent for recipe-driven agents.
 */
public class DefaultMCPAgent extends MCPAgent {
    public DefaultMCPAgent(AgentRecipe recipe, MCPMessage userMessage) {
        super(recipe, userMessage);
    }

    @Override
    public List<String> getAvailableTools() {
        return recipe.getRequiredTools() != null ? recipe.getRequiredTools() : Collections.emptyList();
    }

    @Override
    protected void updateGoalStatus() {
        // No-op for default agent
    }
} 