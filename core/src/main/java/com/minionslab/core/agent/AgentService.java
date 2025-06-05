package com.minionslab.core.agent;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.memory.MemoryFactory;

import com.minionslab.core.memory.MemoryManager;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.SimpleMessage;
import com.minionslab.core.service.ModelCallService;
import com.minionslab.core.step.StepManager;
import io.micrometer.core.instrument.MockClock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AgentService provides the main entry points for running and orchestrating agents in the MCP framework.
 * It coordinates agent instantiation, context creation, memory management, and workflow execution
 * using pluggable chains and strategies.
 * <p>
 * This class is designed for extensibility: you can override or extend it to support custom agent
 * instantiation, memory wiring, or orchestration logic. It supports dynamic agent recipes, user messages,
 * and custom chain registries.
 */
@Service
public class AgentService {
    /**
     * Repository for retrieving agent recipes by ID.
     */
    private final AgentRecipeRepository agentRecipeRepository;
    /**
     * Service for model call orchestration.
     */
    private final ModelCallService modelCallService;
    /**
     * Chain registry for agent and step processing.
     */
    private final ChainRegistry chainRegistry;
    /**
     * Factory for creating memory managers and chains.
     */
    private final MemoryFactory memoryFactory;
    
    /**
     * Constructs an AgentService with the required dependencies.
     *
     * @param agentRecipeRepository the agent recipe repository
     * @param modelCallService the model call service
     * @param chainRegistry the chain registry
     * @param memoryFactory the memory factory
     */
    @Autowired
    public AgentService(
            AgentRecipeRepository agentRecipeRepository,
            ModelCallService modelCallService, ChainRegistry chainRegistry, MemoryFactory memoryFactory) {
        this.agentRecipeRepository = agentRecipeRepository;
        this.modelCallService = modelCallService;
        this.chainRegistry = chainRegistry;
        this.memoryFactory = memoryFactory;
    }
    
    /**
     * Runs an agent with the specified recipe ID.
     *
     * @param recipeId The ID of the recipe to execute
     * @return The results of the agent execution
     * @throws IllegalArgumentException if no recipe is found for the given ID
     */
    public AgentContext runAgent(String recipeId) {
        AgentRecipe recipe = agentRecipeRepository.findById(recipeId);
        if (recipe == null) {
            throw new IllegalArgumentException("No AgentRecipe found for recipeId: " + recipeId);
        }
        return runAgent(recipe);
    }
    
    /**
     * Runs an agent with the specified recipe and a default user message.
     *
     * @param recipe The recipe to execute
     * @return The results of the agent execution
     */
    public AgentContext runAgent(AgentRecipe recipe) {
        return runAgent(recipe, SimpleMessage.builder().content("You are the agent, run your recipe").role(MessageRole.USER).build());
    }
    
    /**
     * Runs an agent with the specified recipe and user message.
     *
     * @param recipe The recipe to execute
     * @param userMessage The user message to start the agent with
     * @return The results of the agent execution
     */
    public AgentContext runAgent(AgentRecipe recipe, Message userMessage) {
        // Use DefaultAgent
        Agent agent = new DefaultAgent(recipe, userMessage);
        // Create ChatModel and contexts
        AgentContext agentContext = createAgentContext(agent);
        return (AgentContext) chainRegistry.process(agentContext);
    }
    
    /**
     * Creates an AgentContext for the given agent, wiring up step and memory managers.
     *
     * @param agent the agent instance
     * @return the agent context
     */
    AgentContext createAgentContext(Agent agent) {
        // Build dependencies
        StepManager stepManager = new StepManager(agent.getRecipe());
        MemoryManager memoryManager = memoryFactory.createMemories(agent.recipe.getMemoryDefintions());
        
        return new AgentContext(agent, stepManager, memoryManager);
    }
    
    /**
     * Runs an agent with the specified recipe and user message string.
     *
     * @param recipe The recipe to execute
     * @param userMessage The user message string
     * @return The results of the agent execution
     */
    public AgentContext runAgent(AgentRecipe recipe, String userMessage) {
        return runAgent(recipe, SimpleMessage.builder().content(userMessage).role(MessageRole.USER).build());
    }
}