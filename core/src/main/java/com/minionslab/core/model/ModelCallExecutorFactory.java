package com.minionslab.core.model;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.model.springai.SpringAIModelCallExecutor;
import com.minionslab.core.service.ChatModelService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class ModelCallExecutorFactory {
    private final ChatModelService chatModelService;
    
    public ModelCallExecutorFactory(ChatModelService chatModelService) {
        this.chatModelService = chatModelService;
    }
    
    public  ModelCallExecutor forProvider(String provider, ModelCall call, AgentContext context) {
        ChatModel model = (ChatModel) chatModelService.getModel(context.getModelConfig());
        if (provider == null || provider.equalsIgnoreCase("openai")) {
            return new SpringAIModelCallExecutor(call, context ,model);
        }
        throw new IllegalArgumentException("Unknown provider: " + provider);
    }
} 