package com.minionslab.core.agent;

import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.step.graph.StepGraph;
import com.minionslab.core.step.graph.StepGraphCompletionStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AgentRecipe defines the configuration and orchestration plan for an MCP agent.
 * It specifies the model, system prompt, memory, tools, step graph, and other parameters
 * that control agent behavior and execution.
 * <p>
 * This class is designed for extensibility: you can add fields for custom configuration,
 * constraints, or orchestration logic. Recipes are used to instantiate and configure agents
 * at runtime, supporting dynamic workflows and pluggable components.
 * <p>
 * To add new configuration options, extend this class or use the builder pattern to add fields.
 * Recipes can be persisted, versioned, and shared across agent instances.
 * <p>
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Add fields for custom configuration, constraints, or orchestration logic.</li>
 *   <li>Extend this class or use the builder pattern to add new configuration options.</li>
 *   <li>Recipes can be persisted, versioned, and shared across agent instances for dynamic workflows.</li>
 * </ul>
 * <b>Usage:</b> Use AgentRecipe to define agent configuration and orchestration plans. Extend or customize for advanced agent behaviors.
 */
@Data
@Accessors(chain = true)
@Builder
@Document(collection = "agent_recipes")
public class AgentRecipe {
    /**
     * Unique recipe ID (for persistence and lookup).
     */
    @Id
    private String id;
    /**
     * The system prompt for the agent (initial context/instructions).
     */
    @NotBlank
    private String systemPrompt;
    /**
     * The model configuration for this agent.
     */
    @NotNull
    private ModelConfig modelConfig;
    /**
     * Arbitrary parameters for extensibility and custom orchestration.
     */
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    /**
     * Constraints for agent execution (e.g., limits, policies).
     */
    @Builder.Default
    private Map<String, Object> constraints = new HashMap<>();
    /**
     * The security policy for this agent.
     */
    private SecurityPolicy securityPolicy;
    /**
     * The list of required tools for this agent.
     */
    @Builder.Default
    private List<String> requiredTools = new ArrayList<>();
    /**
     * The memory type or backend for this agent.
     */
    @Builder.Default
    private String memoryType = "inMemory";
    private StepGraph stepGraph;
    /**
     * stepGraphDefinition: maps stepId to possible next stepIds. Used to build the StepGraph at runtime. Should be a DAG.
     */
    @Builder.Default
    private Map<String, List<String>> stepGraphDefinition = new HashMap<>();
    private MessageBundle messageBundle;
    private List<String> memoryDefinitions;
    private StepGraphCompletionStrategy completionStrategy;
    
    /**
     * Get the step graph for this agent. If not set, can be built dynamically.
     *
     * @return the step graph
     */
    public StepGraph getStepGraph() {
        return stepGraph;
    }
    
    public void setStepGraph(StepGraph stepGraph) {
        this.stepGraph = stepGraph;
    }
    
    public Map<String, List<String>> getStepGraphDefinition() {
        return stepGraphDefinition;
    }
    
    public void setStepGraphDefinition(Map<String, List<String>> stepGraphDefinition) {
        this.stepGraphDefinition = stepGraphDefinition;
    }
    
    public List<String> getMemoryDefintions() {
        return memoryDefinitions;
    }
    
    

}