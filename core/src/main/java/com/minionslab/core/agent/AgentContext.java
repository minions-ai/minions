package com.minionslab.core.agent;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.ProcessResult;
import com.minionslab.core.common.logging.LoggingTopics;
import com.minionslab.core.memory.MemoryManager;
import com.minionslab.core.step.StepManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AgentContext is the unified context object for an agent's execution in the MCP framework.
 * It aggregates all relevant state, configuration, memory, step management, and metadata
 * for a single agent session or conversation.
 * <p>
 * This class is designed for extensibility: you can add fields for custom metadata, tracking,
 * or advanced orchestration. It is the primary carrier for information as agent processors,
 * chains, and steps are executed.
 * <p>
 * AgentContext also implements {@link ProcessContext} to support result aggregation and
 * chain-of-responsibility processing.
 * <p>
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Add fields for custom metadata, tracking, or advanced orchestration.</li>
 *   <li>Override or extend methods to support custom context management or result aggregation.</li>
 *   <li>Use the builder pattern to construct custom AgentContext instances for advanced scenarios.</li>
 * </ul>
 * <b>Usage:</b> AgentContext is the primary carrier for information as agent processors, chains, and steps are executed. It is designed to be extended for advanced agent
 * orchestration and tracking.
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@Slf4j(topic = LoggingTopics.AGENT)
public class AgentContext implements ProcessContext {
    
    private static final ThreadLocal<AgentConfig> configHolder = new ThreadLocal<>();
    /**
     * The agent's configuration/recipe.
     */
    private final AgentRecipe recipe;
    /**
     * The step manager for orchestrating step execution.
     */
    private final StepManager stepManager;
    /**
     * The agent instance for this context.
     */
    private final Agent agent;
    
    /**
     * The memory manager for this agent session.
     */
    private final MemoryManager memoryManager;
    /**
     * Arbitrary metadata for extensibility and custom orchestration.
     */
    private final Map<String, Object> metadata;
    /**
     * The time this context was created.
     */
    private final Instant createdAt;
    /**
     * Whether repeated steps are allowed in this context.
     */
    public boolean allowRepeatedSteps = false;
    /**
     * The last time this context was updated.
     */
    private Instant lastUpdatedAt;
    /**
     * Map of results, keyed by timestamp.
     */
    private Map<Instant, ProcessResult> result = new HashMap<>();
    private ChainRegistry chainRegistry;
    
    /**
     * Constructs an AgentContext for the given agent, step manager, and memory manager.
     *
     * @param agent         the agent instance
     * @param stepManager   the step manager
     * @param memoryManager the memory manager
     */
    public AgentContext(Agent agent, StepManager stepManager, MemoryManager memoryManager) {
        this.recipe = agent.getRecipe();
        this.agent = agent;
        
        this.memoryManager = memoryManager;
        this.createdAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
        this.stepManager = stepManager;
        this.metadata = new HashMap<>();
        initializeDefaultMetadata();
    }
    
    /**
     * Initializes default metadata values for this context.
     */
    private void initializeDefaultMetadata() {
        metadata.putIfAbsent("maxModelCallsPerStep", 10);
        metadata.putIfAbsent("maxToolCallRetries", 2);
        metadata.putIfAbsent("sequentialToolCalls", false);
    }
    
    public static AgentConfig getConfig() {
        return configHolder.get();
    }
    
    public static void setConfig(AgentConfig config) {
        configHolder.set(config);
    }
    
    public static void clear() {
        configHolder.remove();
    }
    
    /**
     * Add a metadata key/value pair to this context.
     *
     * @param key   the metadata key
     * @param value the metadata value
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        updateLastModified();
    }
    
    /**
     * Update the last modified timestamp for this context.
     */
    private void updateLastModified() {
        this.lastUpdatedAt = Instant.now();
    }
    
    /**
     * Get a metadata value by key.
     *
     * @param key the metadata key
     * @return the metadata value, or null if not present
     */
    public Object getMetadataValue(String key) {
        return this.metadata.get(key);
    }
    
    /**
     * Get the conversation ID for this agent context (usually the agent ID).
     *
     * @return the conversation ID
     */
    public String getConversationId() {
        return agent.getAgentId();
    }
    
    /**
     * Get the list of results accumulated during agent execution.
     *
     * @return the list of results
     */
    @Override
    public List<ProcessResult> getResults() {
        return result.values().stream().toList();
    }
    
    /**
     * Add a result to this context, keyed by the current timestamp.
     *
     * @param result the result to add
     */
    @Override
    public void addResult(ProcessResult result) {
        this.result.put(Instant.now(), result);
    }
    
    
}