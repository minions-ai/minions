package com.minionslab.mcp.context;


import com.minionslab.mcp.agent.AgentRecipe;
import com.minionslab.mcp.config.ModelConfig;
import com.minionslab.mcp.message.MCPMessage;
import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.model.ModelCallExecutionContext;
import com.minionslab.mcp.step.MCPStep;
import com.minionslab.mcp.step.StepExecution;
import com.minionslab.mcp.tool.ToolCallExecutionContext;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

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
    @NotBlank
    private String agentId;
    
    
    private ModelConfig modelConfig;
//
//    @Builder.Default
//    private List<ToolDefinition> availableTools = new ArrayList<>();
    
    @Builder.Default
    private List<MCPStep> executionSteps = new ArrayList<>();
    
    @Builder.Default
    private List<MCPModelCall> modelCalls = new ArrayList<>();
    
    @Builder.Default
    private List<MCPMessage> messages = new ArrayList<>();
    
    @Builder.Default
    private Map<String, Object> memory = new HashMap<>();
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    private ModelCallExecutionContext modelCallExecutionContext;
    
    private ToolCallExecutionContext toolCallExecutionContext;
    
    @Builder.Default
    private List<MCPStep.StepInstruction> instructions = new ArrayList<>();
    
    
    private Instant createdAt;
    private Instant lastUpdatedAt;
    private Optional<MCPStep> nextStep;
    
    
    public MCPContext(String agentId, AgentRecipe recipe) {
        this.recipe = recipe;
        this.agentId = agentId;
        this.modelConfig = recipe.getModelConfig();
        this.messages = new ArrayList<>();
        this.executionSteps = new ArrayList<>();
        this.memory = new HashMap<>();
        this.metadata = new HashMap<>();
//        this.availableTools = new ArrayList<>();
        this.createdAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
    }
    
    
    /**
     * Adds a message to the context.
     *
     * @param message The message to add
     */
    public void addMessage(MCPMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        this.messages.add(message);
        updateLastModified();
    }
    
    private void updateLastModified() {
        this.lastUpdatedAt = Instant.now();
    }
    
    /**
     * Adds a step to the context.
     *
     * @param step The step to add
     */
    public void addStep(MCPStep step) {
        if (step == null) {
            throw new IllegalArgumentException("MCPStep cannot be null");
        }
        this.executionSteps.add(step);
        updateLastModified();
    }
    
    
    /**
     * Adds metadata to the context.
     *
     * @param key   The metadata key
     * @param value The metadata value
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        updateLastModified();
    }
    
    /**
     * Adds a value to the agent's memory.
     *
     * @param key   The memory key
     * @param value The memory value
     */
    public void addToMemory(String key, Object value) {
        this.memory.put(key, value);
        updateLastModified();
    }
    
    /**
     * Gets a specific metadata value.
     *
     * @param key The metadata key
     * @return The metadata value or null if not found
     */
    public Object getMetadataValue(String key) {
        return this.metadata.get(key);
    }
    
    /**
     * Gets a value from the agent's memory.
     *
     * @param key The memory key
     * @return The memory value or null if not found
     */
    public Object getFromMemory(String key) {
        return this.memory.get(key);
    }
    
    
    /**
     * Gets all StepExecution objects for the steps in this context.
     *
     * @return List of StepExecution objects (non-null only)
     */
    public List<StepExecution> getStepExecutions() {
        return executionSteps.stream()
                             .map(step -> step.getStepExecution())
                             .filter(java.util.Objects::nonNull)
                             .toList();
    }
    
    @Override
    public String toString() {
        return "MCPContext{" +
                       "agentId='" + agentId + '\'' +
                       ", modelId='" + modelConfig.getModelId() + '\'' +
                       ", messageCount=" + messages.size() +
                       ", stepCount=" + executionSteps.size() +
//                       ", toolCount=" + availableTools.size() +
                       ", metadataKeys=" + metadata.keySet() +
                       ", memoryKeys=" + memory.keySet() +
                       ", createdAt=" + createdAt +
                       ", lastUpdatedAt=" + lastUpdatedAt +
                       '}';
    }
    
    public void addInstruction(MCPStep.StepInstruction stepInstruction) {
        this.instructions.add(stepInstruction);
    }
    
    public void setNextStep(String s) {
        nextStep = this.getExecutionSteps().stream().filter(mcpStep -> mcpStep.getId().equals(s)).findAny();
    }
    
    public void clearNextStep() {
        this.nextStep = Optional.empty();
    }
}