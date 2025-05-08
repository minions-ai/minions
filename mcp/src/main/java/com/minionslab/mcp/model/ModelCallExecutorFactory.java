package com.minionslab.mcp.model;

import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.model.springai.SpringAIModelCallExecutor;
import com.minionslab.mcp.service.ChatModelService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.Model;
import org.springframework.stereotype.Component;

@Component
public class ModelCallExecutorFactory {
    private final ChatModelService chatModelService;
    
    public ModelCallExecutorFactory(ChatModelService chatModelService) {
        this.chatModelService = chatModelService;
    }
    
    public  ModelCallExecutor forProvider(String provider, MCPModelCall call, MCPContext context) {
        ChatModel model = (ChatModel) chatModelService.getModel(context.getModelConfig());
        if (provider == null || provider.equalsIgnoreCase("spring")) {
            return new SpringAIModelCallExecutor(call, context ,model);
        }
        throw new IllegalArgumentException("Unknown provider: " + provider);
    }
} 