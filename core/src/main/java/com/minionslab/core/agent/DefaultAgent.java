package com.minionslab.core.agent;

import com.minionslab.core.common.message.Message;

import java.util.Collections;
import java.util.List;

/**
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Extend DefaultAgent for recipe-driven agents with custom tool logic or behaviors.</li>
 *   <li>Override {@link #getAvailableTools()} to customize tool selection.</li>
 * </ul>
 * <b>Usage:</b> Use DefaultAgent as a base for simple, recipe-driven agent implementations.
 */
public class DefaultAgent extends Agent {
    public DefaultAgent(AgentRecipe recipe, Message userMessage) {
        super(recipe, userMessage);
    }

    @Override
    public List<String> getAvailableTools() {
        return recipe.getRequiredTools() != null ? recipe.getRequiredTools() : Collections.emptyList();
    }


} 