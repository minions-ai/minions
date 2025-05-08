package com.minionslab.mcp.agent;

import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.memory.MCPChatMemory;
import com.minionslab.mcp.memory.MCPChatMemoryFactory;
import com.minionslab.mcp.message.DefaultMCPMessage;
import com.minionslab.mcp.message.MCPMessage;
import com.minionslab.mcp.message.MessageRole;
import com.minionslab.mcp.model.ModelCallExecutorFactory;
import com.minionslab.mcp.service.ChatModelService;
import com.minionslab.mcp.step.StepManager;
import com.minionslab.mcp.tool.ToolCallExecutorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentService {
    private final AgentRecipeRepository agentRecipeRepository;
    private final ChatModelService chatModelService;
    private final MCPChatMemoryFactory mCPChatMemoryFactory;
    private final ModelCallExecutorFactory modelCallExecutorFactory;
    private final ToolCallExecutorFactory toolCallExecutorFactory;
    
    @Autowired
    public AgentService(
            AgentRecipeRepository agentRecipeRepository,
            ChatModelService chatModelService, MCPChatMemoryFactory mCPChatMemoryFactory, ModelCallExecutorFactory modelCallExecutorFactory, ToolCallExecutorFactory toolCallExecutorFactory) {
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
        return runAgent(recipe, DefaultMCPMessage.builder().content("You are the agent, run your recipe").role(MessageRole.USER).build());
    }
    
    /**
     * Runs an agent with the specified recipe.
     *
     * @param recipe The recipe to execute
     * @return The result of the agent execution
     */
    public AgentResult runAgent(AgentRecipe recipe, MCPMessage userMessage) {
        // Create ChatModel and contexts
        MCPContext agentContext = createAgentContext(recipe);
        
        // Use DefaultMCPAgent
        MCPAgent agent = new DefaultMCPAgent(recipe, userMessage);
        
        // Create executor and run
        AgentExecutor agentExecutor = new AgentExecutor(agent, agentContext,modelCallExecutorFactory,toolCallExecutorFactory);
        return agentExecutor.executeSync();
    }
    
    private MCPContext createAgentContext(AgentRecipe recipe) {
        // Build dependencies
        StepManager stepManager = new StepManager(recipe);
        MCPChatMemory chatMemory = mCPChatMemoryFactory.create(recipe);
        return new MCPContext(recipe.getId(), recipe, stepManager, chatMemory);
    }
    
    
    public AgentResult runAgent(AgentRecipe recipe, String userMessage) {
        
        return runAgent(recipe, DefaultMCPMessage.builder().content(userMessage).role(MessageRole.USER).build());
    }
}