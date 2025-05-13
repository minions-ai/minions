package com.minionslab.core.agent;

import com.minionslab.core.message.Message;

import java.util.Collections;
import java.util.List;

/**
 * Default concrete implementation of Agent for recipe-driven agents.
 */
public class DefaultMCPAgent extends Agent {
    public DefaultMCPAgent(AgentRecipe recipe, Message userMessage) {
        super(recipe, userMessage);
    }

    @Override
    public List<String> getAvailableTools() {
        return recipe.getRequiredTools() != null ? recipe.getRequiredTools() : Collections.emptyList();
    }


} 