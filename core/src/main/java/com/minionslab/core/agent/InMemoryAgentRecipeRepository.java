package com.minionslab.core.agent;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAgentRecipeRepository implements AgentRecipeRepository {
    private final Map<String, AgentRecipe> recipes = new ConcurrentHashMap<>();

    @Override
    public AgentRecipe findById(String agentId) {
        return recipes.get(agentId);
    }

    public void save(String agentId, AgentRecipe recipe) {
        recipes.put(agentId, recipe);
    }
} 