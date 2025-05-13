package com.minionslab.core.service;

import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.model.ModelInfo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.Model;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChatModelService {
    private final ListableBeanFactory beanFactory;
    private final Map<String, ModelInfo> modelInfoCache = new ConcurrentHashMap<>();
    
    public ChatModelService(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    
    @PostConstruct
    public void initialize() {
        // Scan for all Model beans
        Map<String, Model> models = beanFactory.getBeansOfType(Model.class);
        log.info("Found {} Model beans", models.size());
        models.forEach((name, model) -> {
            ModelInfo info = ModelInfo.from(name, model);
            log.info("Caching ModelInfo: {}", info);
            modelInfoCache.put(name, info);
        });
    }
    
    /**
     * Gets a ChatModel based on the provided ModelConfig.
     * This will return a cached model if one exists that matches the configuration,
     * or throw if not found.
     *
     * @param config The model configuration
     * @return A ChatModel instance
     * @throws IllegalArgumentException if no suitable model can be found
     */
    public Model getModel(ModelConfig config) {
        ModelInfo info = getModelInfo(config.getProvider(), config.getModelId());
        if (info != null) {
            return info.model();
        }
        throw new IllegalArgumentException(
                String.format("No ChatModel found for configuration: provider=%s, model=%s, version=%s",
                        config.getProvider(), config.getModelId(), config.getVersion())
        );
    }

    /**
     * Retrieves ModelInfo by provider and modelId (case-insensitive).
     */
    public ModelInfo getModelInfo(String provider, String modelId) {
        return modelInfoCache.values().stream()
                .filter(info -> info.provider().equalsIgnoreCase(provider)
                        && info.modelId().equalsIgnoreCase(modelId))
                .findFirst()
                .orElse(null);
    }

    public Set<String> getModelNames() {
        return modelInfoCache.keySet();
    }

    public void clearCache() {
        modelInfoCache.clear();
    }

    public int getCacheSize() {
        return modelInfoCache.size();
    }
}
