package com.minionslab.core.agent;

import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.step.StepGraph;
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
 * Configuration class for MCP agents.
 */
@Data
@Accessors(chain = true)
@Builder
@Document(collection = "agent_recipes")
public class AgentRecipe {
    
    @Id
    private String id;
    @NotBlank
    private String systemPrompt;
    @NotNull
    private ModelConfig modelConfig;
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    @Builder.Default
    private Map<String, Object> constraints = new HashMap<>();
    private SecurityPolicy securityPolicy;
    
    @Builder.Default
    private List<String> availableTools = new ArrayList<>();
    
    @Builder.Default
    private List<String> requiredTools = new ArrayList<>();
    ;
    @Builder.Default
    private String memoryType = "inMemory";
    
    private StepGraph stepGraph;
    
    /**
     * stepGraphDefinition: maps stepId to possible next stepIds. Used to build the StepGraph at runtime. Should be a DAG.
     */
    @Builder.Default
    private Map<String, List<String>> stepGraphDefinition = new HashMap<>();
    private MessageBundle messageBundle;
    
    /**
     * stepGraph: maps stepId to possible next stepIds. If not set, can be built dynamically. Should be a DAG.
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
    
    
}