package com.minionslab.core.agent;

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


} 