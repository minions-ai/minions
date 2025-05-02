package com.minionslab.mcp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.mcp.config.ModelConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.Model;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChatModelService {
    private final ListableBeanFactory beanFactory;
    private final Map<String, Model> modelCache = new ConcurrentHashMap<>();
    
    public ChatModelService(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    
    @PostConstruct
    public void initialize() {
        // Scan for all ChatModel beans
        Map<String, Model> models = beanFactory.getBeansOfType(Model.class);
        log.info("Found {} ChatModel beans", models.size());
        
        // Cache them
        models.forEach((name, model) -> {
            log.info("Caching ChatModel bean: {}", name);
            modelCache.put(name, model);
        });
    }
    
    
    /**
     * Gets a ChatModel based on the provided ModelConfig.
     * This will return a cached model if one exists that matches the configuration,
     * or create a new one if needed.
     *
     * @param config The model configuration
     * @return A ChatModel instance
     * @throws IllegalArgumentException if no suitable model can be found or created
     */
    public Model getModel(ModelConfig config) {
        String cacheKey = createCacheKey(config);
        
        // Try to get from cache first
        Model model = modelCache.get(cacheKey);
        if (model != null) {
            return model;
        }
        
        // If not in cache, look for a bean with matching configuration
        Map<String, ChatModel> chatModels = beanFactory.getBeansOfType(ChatModel.class);
        for (ChatModel candidate : chatModels.values()) {
            if (modelMatchesConfig(candidate, config)) {
                modelCache.put(cacheKey, candidate);
                return candidate;
            }
        }
        
        throw new IllegalArgumentException(
                String.format("No ChatModel found for configuration: provider=%s, model=%s, version=%s",
                        config.getProvider(), config.getModelId(), config.getVersion())
        );
    }
    
    private String createCacheKey(ModelConfig config) {
        return config.getModelId();
    }
    
    private boolean modelMatchesConfig(ChatModel model, ModelConfig config) {
        // This is a basic implementation. You might want to add more sophisticated matching logic
        String modelName = model.getClass().getSimpleName().toLowerCase();
        return modelName.contains(config.getProvider().toLowerCase()) &&
                       modelName.contains(config.getModelId().toLowerCase());
    }
    
    public Set<String> getModelNames() {
        return modelCache.keySet();
    }
    
    /**
     * Clears the model cache.
     */
    public void clearCache() {
        modelCache.clear();
    }
    
    /**
     * Gets the number of cached models.
     *
     * @return The size of the model cache
     */
    public int getCacheSize() {
        return modelCache.size();
    }
    
    

}
