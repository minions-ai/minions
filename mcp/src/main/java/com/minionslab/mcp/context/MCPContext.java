package com.minionslab.mcp.context;

import com.minionslab.mcp.agent.AgentRecipe;
import com.minionslab.mcp.config.ModelConfig;
import com.minionslab.mcp.memory.MCPChatMemory;
import com.minionslab.mcp.step.StepManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.*;

/**
 * Unified context class that represents all context information for an agent's conversation and state.
 * This includes model configuration, available tools, execution steps, memory, messages,
 * and other metadata.
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
public class MCPContext {
    private final AgentRecipe recipe;
    public boolean allowRepeatedSteps = false;
    @NotBlank
    private String agentId;
    private ModelConfig modelConfig;
    @Builder.Default
    private Map<String, Object> memory = new HashMap<>();
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private Instant createdAt;
    private Instant lastUpdatedAt;
    @Builder.Default
    private List<String> availableTools = new ArrayList<>();
    private final StepManager stepManager;
    private final MCPChatMemory chatMemory;

    public MCPContext(String agentId, AgentRecipe recipe, StepManager stepManager, MCPChatMemory chatMemory) {
        this.recipe = recipe;
        this.agentId = agentId;
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

    private void updateLastModified() {
        this.lastUpdatedAt = Instant.now();
    }

    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        updateLastModified();
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
        return "MCPContext{" +
                "agentId='" + agentId + '\'' +
                ", modelId='" + modelConfig.getModelId() + '\'' +
                ", metadataKeys=" + metadata.keySet() +
                ", memoryKeys=" + memory.keySet() +
                ", createdAt=" + createdAt +
                ", lastUpdatedAt=" + lastUpdatedAt +
                '}';
    }

    // Only expose getters for stepManager and chatMemory
    public StepManager getStepManager() { return stepManager; }
    public MCPChatMemory getChatMemory() { return chatMemory; }
}