package com.minionslab.core.agent;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for retrieving AgentConfig by agentId.
 */
public interface AgentRecipeRepository {
    AgentRecipe findById(String agentId);

    /**
     * Simple in-memory implementation for demonstration/testing.
     */
    @Repository
    class InMemoryAgentConfigRepository implements AgentRecipeRepository {
        private final Map<String, AgentRecipe> configMap = new ConcurrentHashMap<>();

        @Override
        public AgentRecipe findById(String agentId) {
            return configMap.get(agentId);
        }

        public void save(String agentId, AgentRecipe config) {
            configMap.put(agentId, config);
        }
    }
} 