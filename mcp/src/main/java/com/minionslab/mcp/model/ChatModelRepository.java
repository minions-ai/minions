package com.minionslab.mcp.model;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatModelRepository {
    private final ApplicationContext applicationContext;

    @Autowired
    public ChatModelRepository(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Resolves a ChatModel bean by provider and model name.
     * @param provider the provider name (e.g., "openai", "anthropic")
     * @param modelName the model name (e.g., "gpt-4o", "claude-3")
     * @return the ChatModel bean
     * @throws IllegalArgumentException if no matching ChatModel is found
     */
    public ChatModel getChatModel(String provider, String modelName) {
        var beans = applicationContext.getBeansOfType(ChatModel.class);
        for (var entry : beans.entrySet()) {
            String beanName = entry.getKey().toLowerCase();
            ChatModel model = entry.getValue();
            String className = model.getClass().getSimpleName().toLowerCase();
            if ((beanName.contains(provider.toLowerCase()) || className.contains(provider.toLowerCase())) &&
                (beanName.contains(modelName.toLowerCase()) || className.contains(modelName.toLowerCase()))) {
                return model;
            }
        }
        throw new IllegalArgumentException("No ChatModel found for provider: " + provider + ", model: " + modelName);
    }
}
