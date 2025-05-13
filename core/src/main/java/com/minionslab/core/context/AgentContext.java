package com.minionslab.core.context;

import com.minionslab.core.agent.Agent;
import com.minionslab.core.agent.AgentRecipe;
import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.memory.ModelMemory;
import com.minionslab.core.step.StepManager;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified context class that represents all context information for an agent's conversation and state.
 * This includes model configuration, available tools, execution steps, memory, messages,
 * and other metadata.
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
public class AgentContext {
    private final AgentRecipe recipe;
    private final StepManager stepManager;
    private final ModelMemory chatMemory;
    public boolean allowRepeatedSteps = false;
    @NotBlank
    private Agent agent;
    private ModelConfig modelConfig;
    @Builder.Default
    private Map<String, Object> memory = new HashMap<>();
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private Instant createdAt;
    private Instant lastUpdatedAt;
    @Builder.Default
    private List<String> availableTools = new ArrayList<>();
    
    
    public AgentContext(Agent agent, StepManager stepManager, ModelMemory chatMemory) {
        this.recipe = agent.getRecipe();
        this.agent = agent;
        this.modelConfig = recipe.getModelConfig();
        this.memory = new HashMap<>();
        this.metadata = new HashMap<>();
        this.availableTools = new ArrayList<>();
        this.availableTools.addAll(recipe.getRequiredTools());
        this.createdAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
        this.stepManager = stepManager;
        this.chatMemory = chatMemory;
        initializeDefaultMetadata();
    }
    
    private void initializeDefaultMetadata() {
        metadata.putIfAbsent("maxModelCallsPerStep", 10);
        metadata.putIfAbsent("maxToolCallRetries", 2);
        metadata.putIfAbsent("sequentialToolCalls", false);
    }
    
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        updateLastModified();
    }
    
    private void updateLastModified() {
        this.lastUpdatedAt = Instant.now();
    }
    
    public void addToMemory(String key, Object value) {
        this.memory.put(key, value);
        updateLastModified();
    }
    
    public Object getMetadataValue(String key) {
        return this.metadata.get(key);
    }
    
    public Object getFromMemory(String key) {
        return this.memory.get(key);
    }
    
    @Override
    public String toString() {
        return "AgentContext{" +
                       "agentId='" + agent.getAgentId() + '\'' +
                       ", modelId='" + modelConfig.getModelId() + '\'' +
                       ", metadataKeys=" + metadata.keySet() +
                       ", memoryKeys=" + memory.keySet() +
                       ", createdAt=" + createdAt +
                       ", lastUpdatedAt=" + lastUpdatedAt +
                       '}';
    }
    
    // Only expose getters for stepManager and chatMemory
    public StepManager getStepManager() {
        return stepManager;
    }
    
    public ModelMemory getChatMemory() {
        return chatMemory;
    }
    
    public String getConversationid() {
        return getAgent().getAgentId();
    }
}