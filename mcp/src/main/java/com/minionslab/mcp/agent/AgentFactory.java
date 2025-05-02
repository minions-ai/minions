package com.minionslab.mcp.agent;

import com.minionslab.mcp.tool.MCPToolOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AgentFactory {
    
    private final MCPToolOrchestrator toolOrchestrator;
    private final ChatClient.Builder chatClientBuilder;
    private final Map<String, Class<? extends MCPAgent>> agentTypeRegistry;
    
    public AgentFactory(
            MCPToolOrchestrator toolOrchestrator,
            ChatClient.Builder chatClientBuilder) {
        this.toolOrchestrator = toolOrchestrator;
        this.chatClientBuilder = chatClientBuilder;
        this.agentTypeRegistry = new ConcurrentHashMap<>();
        
        // Register default agent types
        registerDefaultAgentTypes();
    }
    
    private void registerDefaultAgentTypes() {

    }
    
    /**
     * Registers a new agent type that can be created by this factory.
     *
     * @param type       The type identifier for the agent
     * @param agentClass The agent class to register
     */
    public void registerAgentType(String type, Class<? extends MCPAgent> agentClass) {
        agentTypeRegistry.put(type, agentClass);
        log.info("Registered agent type: {}", type);
    }
    
    /**
     * Creates and initializes an MCPAgent based on the provided recipe.
     *
     * @param recipe The recipe to use for agent creation
     * @return An initialized MCPAgent instance
     * @throws IllegalArgumentException if the agent type is not registered
     */
    public MCPAgent createAgent(AgentRecipe recipe) {
        String agentType = recipe.getParameters().getOrDefault("agentType", "default").toString();
        Class<? extends MCPAgent> agentClass = agentTypeRegistry.get(agentType);
        
        if (agentClass == null) {
            throw new IllegalArgumentException("No agent type registered for: " + agentType);
        }
        
        try {
            // Create agent instance
            MCPAgent agent = agentClass.getDeclaredConstructor(
                    MCPToolOrchestrator.class,
                    ChatClient.Builder.class).newInstance(toolOrchestrator, chatClientBuilder);
            
            // Initialize the agent with the recipe
            agent.initialize(recipe);
            
            log.info("Created and initialized agent of type: {}", agentType);
            return agent;
            
        } catch (Exception e) {
            throw new AgentCreationException("Failed to create agent of type: " + agentType, e);
        }
    }
    
    /**
     * Exception thrown when agent creation fails.
     */
    public static class AgentCreationException extends RuntimeException {
        public AgentCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 