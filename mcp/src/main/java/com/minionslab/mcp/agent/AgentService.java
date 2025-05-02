package com.minionslab.mcp.agent;

import com.minionslab.mcp.config.ModelConfig;
import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.message.DefaultMCPMessage;
import com.minionslab.mcp.message.MessageRole;
import com.minionslab.mcp.model.ModelCallExecutionContext;
import com.minionslab.mcp.service.ChatModelService;
import com.minionslab.mcp.tool.ToolCallExecutionContext;
import com.minionslab.mcp.util.MCPMessageSpringConverter;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentService {
    private final AgentRecipeRepository agentRecipeRepository;
    private final ChatModelService chatModelService;
    private final AgentFactory agentFactory;
    
    @Autowired
    public AgentService(
            AgentRecipeRepository agentRecipeRepository,
            ChatModelService chatModelService,
            AgentFactory agentFactory) {
        this.agentRecipeRepository = agentRecipeRepository;
        this.chatModelService = chatModelService;
        this.agentFactory = agentFactory;
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
    
    /**
     * Runs an agent with the specified recipe.
     *
     * @param recipe The recipe to execute
     * @return The result of the agent execution
     */
    public AgentResult runAgent(AgentRecipe recipe) {
        // Create ChatModel and contexts
        ChatModel chatModel = (ChatModel) chatModelService.getModel(recipe.getModelConfig());
        MCPContext agentContext = createAgentContext(recipe);
        ModelCallExecutionContext modelContext = createModelContext(recipe.getId(), chatModel, recipe);
        ToolCallExecutionContext toolContext = createToolContext(recipe, chatModel);
        
        // Create and initialize the agent
        MCPAgent agent = agentFactory.createAgent(recipe);
        
        // Create executor and run
        AgentExecutor agentExecutor = new AgentExecutor(agent);
        return agentExecutor.executeSync();
    }
    
    private MCPContext createAgentContext(AgentRecipe recipe) {
        MCPContext context = new MCPContext(recipe.getId(), recipe.getModelConfig());
        if (recipe.getSystemPrompt() != null) {
            context.addMessage(DefaultMCPMessage.builder().content(recipe.getSystemPrompt()).role(MessageRole.SYSTEM).build());
        }
        return context;
    }
    
    private ModelCallExecutionContext createModelContext(String conversationId, ChatModel chatModel, AgentRecipe recipe) {
        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                .temperature(recipe.getModelConfig().getTemperature())
                .topP(recipe.getModelConfig().getTopP())
                .maxTokens(recipe.getModelConfig().getMaxTokens())
                .build();
        
        return ModelCallExecutionContext.builder()
                                        .chatModel(chatModel)
                                        .chatMemory(recipe.getMemoryRepository())
                                        .conversationId(conversationId)
                                        .chatOptions(chatOptions)
                                        .messageConverter(new MCPMessageSpringConverter())
                                        .build();
    }
    
    private ToolCallExecutionContext createToolContext(AgentRecipe recipe, ChatModel chatModel) {
        ToolCallingManager toolCallingManager = DefaultToolCallingManager.builder()
                .build();
        
        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                                                     .toolCallbacks()
                .temperature(recipe.getModelConfig().getTemperature())
                .topP(recipe.getModelConfig().getTopP())
                .maxTokens(recipe.getModelConfig().getMaxTokens())
                .build();
        
        return ToolCallExecutionContext.builder()
                                       .chatMemory(recipe.getMemoryRepository())
                                       .chatOptions(chatOptions)
                                       .chatModel(chatModel)
                                       .conversationId(recipe.getId())
                                       .toolCallingManager(toolCallingManager)
                                       .build();
    }
} 