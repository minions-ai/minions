package com.minionslab.core.agent;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Implement this interface to provide custom persistence or retrieval logic for agent recipes.</li>
 *   <li>Use the provided in-memory implementation for testing or demonstration.</li>
 *   <li>Extend with additional query methods as needed for your application.</li>
 * </ul>
 * <b>Usage:</b> Use AgentRecipeRepository to retrieve and manage agent recipes. Plug in your own implementation for production use.
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