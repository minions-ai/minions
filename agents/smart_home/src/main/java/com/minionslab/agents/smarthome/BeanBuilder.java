package com.minionslab.agents.smarthome;

import com.minionslab.core.agent.AgentRecipe;
import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.common.message.MessageRole;
import com.minionslab.core.common.message.MessageScope;
import com.minionslab.core.step.graph.DefaultStepGraph;
import com.minionslab.core.step.graph.DefaultStepGraphDefinition;
import com.minionslab.core.step.graph.StepGraph;
import com.minionslab.core.step.graph.StepGraphCompletionStrategy;
import com.minionslab.core.step.impl.PlannerStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class BeanBuilder {
    @Bean
    public AgentRecipe getAgentRecipe() {
        // The following can be used as a Spring bean or initialized elsewhere as needed
        AgentRecipe recipe = AgentRecipe.builder()
                                        .id("smart_home_helper")
                                        .memoryDefinitions(List.of())
                                        .modelConfig(getModelConfig())
                                        .completionStrategy(getCompletionStrategy())
                                        .systemPrompt(getSystemPrompt())
                                        .stepGraph(getStepGraph())
                                        .requiredTools(getRequiredTools())
                                        .build();
        return recipe;
    }
    
    private ModelConfig getModelConfig() {
        return ModelConfig.builder()
                          .modelId("gpt-4o")
                          .provider("openai")
                          .version("2024-01-01")
                          .maxContextLength(4096)
                          .maxTokens(512)
                          .temperature(0.7)
                          .topP(1.0)
                          .build();
    }
    
    private StepGraphCompletionStrategy getCompletionStrategy() {
        // Simple strategy: complete when there are no more steps
        return (graph, currentStep, context) -> graph.getNextStep(context) == null;
    }
    
    private String getSystemPrompt() {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        resourceLoader.setClassLoader(BeanBuilder.class.getClassLoader());
        try {
            String filename = resourceLoader.getResource("classpath:system_prompt.txt").getContentAsString(Charset.defaultCharset());
            log.info("System Prompt: {}", filename);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    private StepGraph getStepGraph() {
        // Minimal example: a single planner step
        PlannerStep plannerStep = new PlannerStep();
        plannerStep.setId("planner");
        plannerStep.setGoal(new DefaultMessage(MessageScope.AGENT, MessageRole.GOAL, "Plan and execute smart home tasks", Collections.emptyMap()));
        plannerStep.setSystemPrompt(new DefaultMessage(MessageScope.AGENT, MessageRole.SYSTEM, getSystemPrompt(), Collections.emptyMap()));
        DefaultStepGraphDefinition def = new DefaultStepGraphDefinition();
        
        def.setStartStep(plannerStep);
        return new DefaultStepGraph(def);
    }
    
    private List<String> getRequiredTools() {
        // Add tool names as needed, e.g., "light_control", "thermostat_control"
        return List.of();
    }
}
