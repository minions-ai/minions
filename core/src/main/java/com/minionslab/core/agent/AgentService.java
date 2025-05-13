package com.minionslab.core.agent;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.memory.ModelMemory;
import com.minionslab.core.memory.ModelMemoryFactory;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.model.ModelCallExecutorFactory;
import com.minionslab.core.service.ChatModelService;
import com.minionslab.core.step.StepManager;
import com.minionslab.core.tool.ToolCallExecutorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentService {
    private final AgentRecipeRepository agentRecipeRepository;
    private final ChatModelService chatModelService;
    private final ModelMemoryFactory mCPChatMemoryFactory;
    private final ModelCallExecutorFactory modelCallExecutorFactory;
    private final ToolCallExecutorFactory toolCallExecutorFactory;
    
    @Autowired
    public AgentService(
            AgentRecipeRepository agentRecipeRepository,
            ChatModelService chatModelService, ModelMemoryFactory mCPChatMemoryFactory, ModelCallExecutorFactory modelCallExecutorFactory,
            ToolCallExecutorFactory toolCallExecutorFactory) {
        this.agentRecipeRepository = agentRecipeRepository;
        this.chatModelService = chatModelService;
        this.mCPChatMemoryFactory = mCPChatMemoryFactory;
        this.modelCallExecutorFactory = modelCallExecutorFactory;
        this.toolCallExecutorFactory = toolCallExecutorFactory;
    }
    
    /**
     * Runs an agent with the specified recipe ID.
     *
     * @param recipeId The ID of the recipe to execute
     * @return The result of the agent execution
     */
    public AgentResult runAgent(String recipeId) {
        AgentRecipe recipe = agentRecipeRepository.findById(recipeId);
        if (recipe == null) {
            throw new IllegalArgumentException("No AgentRecipe found for recipeId: " + recipeId);
        }
        return runAgent(recipe);
    }
    
    public AgentResult runAgent(AgentRecipe recipe) {
        return runAgent(recipe, DefaultMessage.builder().content("You are the agent, run your recipe").role(MessageRole.USER).build());
    }
    
    /**
     * Runs an agent with the specified recipe.
     *
     * @param recipe The recipe to execute
     * @return The result of the agent execution
     */
    public AgentResult runAgent(AgentRecipe recipe, Message userMessage) {
        // Use DefaultMCPAgent
        Agent agent = new DefaultMCPAgent(recipe, userMessage);
        // Create ChatModel and contexts
        AgentContext agentContext = createAgentContext(agent);
        
        
        // Create executor and run
        AgentExecutor agentExecutor = new AgentExecutor(agentContext, modelCallExecutorFactory, toolCallExecutorFactory);
        return agentExecutor.execute();
    }
    
    private AgentContext createAgentContext(Agent agent) {
        // Build dependencies
        
        StepManager stepManager = new StepManager(agent.getRecipe());
        ModelMemory chatMemory = mCPChatMemoryFactory.create(agent);
        return new AgentContext(agent, stepManager, chatMemory);
    }
    
    
    public AgentResult runAgent(AgentRecipe recipe, String userMessage) {
        
        return runAgent(recipe, DefaultMessage.builder().content(userMessage).role(MessageRole.USER).build());
    }
}