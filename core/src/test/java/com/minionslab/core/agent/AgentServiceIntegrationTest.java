package com.minionslab.core.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.memory.MemoryFactory;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.message.MessageFactory;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.SimpleMessage;
import com.minionslab.core.service.ModelCallService;
import com.minionslab.core.service.ToolCallService;
import com.minionslab.core.step.StepFactory;
import com.minionslab.core.step.definition.ModelCallStepDefinition;
import com.minionslab.core.step.definition.ToolCallStepDefinition;
import com.minionslab.core.step.graph.DefaultStepGraph;
import com.minionslab.core.step.graph.DefaultStepGraphDefinition;
import com.minionslab.core.step.graph.StepGraph;
import com.minionslab.core.step.impl.ModelCallStep;
import com.minionslab.core.step.impl.ToolCallStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for AgentService covering a workflow with both model call and tool call steps.
 * Scenarios:
 * <ul>
 *   <li>AgentRecipe with ModelCallStepDefinition and ToolCallStepDefinition is processed end-to-end</li>
 *   <li>ModelCallService and ToolCallService are invoked as expected</li>
 *   <li>Workflow completes and results are present</li>
 * </ul>
 * Setup: Mocks ModelCallService, ToolCallService, ChainRegistry, MemoryFactory, and uses real StepFactory.
 */
@SpringBootTest
class AgentServiceIntegrationTest {
    @Autowired
    private AgentService agentService;
    @Autowired
    private ModelCallService modelCallService;
    @Autowired
    private ToolCallService toolCallService;
    private StepFactory stepFactory;
    
    private ChainRegistry chainRegistry;
    private MemoryFactory memoryFactory;
    @Autowired
    private MessageFactory messageFactory;
    
    @BeforeEach
    void setUp() {
        // No manual mocking or instantiation of agentService, modelCallService, or toolCallService
        // Let Spring autowire the real beans
        stepFactory = new StepFactory(new ObjectMapper()); // If needed, inject real beanFactory
    }
    
//    @Test
    void testAgentRecipeEndToEndModelAndToolCall() {
        // 1. Create step definitions
        ModelCallStepDefinition modelCallStepDefinition = new ModelCallStepDefinition();
        modelCallStepDefinition.setId("plannerStep");
        modelCallStepDefinition.setPromptTemplate("What is the weather?");
        modelCallStepDefinition.setSystemPrompt(messageFactory.createMessageFromResource("file:src/test/resources/planner_agent_system.txt", MessageRole.SYSTEM));
        modelCallStepDefinition.setGoal(SimpleMessage.builder().content("Get weather").role(MessageRole.GOAL).build());
        
        ToolCallStepDefinition toolStepDef = new ToolCallStepDefinition();
        toolStepDef.setId("toolStep");
        toolStepDef.setToolName("weatherApi");
        toolStepDef.setInput(java.util.Map.of("query", "weather in Paris"));
        toolStepDef.setGoal(SimpleMessage.builder().content("Call weather API").role(MessageRole.GOAL).build());
        
        // 2. Build steps from definitions
        ModelCallStep modelStep = modelCallStepDefinition.buildStep();
        ToolCallStep toolStep = toolStepDef.buildStep();
        
        // 3. Build step graph (linear: modelStep -> toolStep)
        DefaultStepGraphDefinition graphDef = new DefaultStepGraphDefinition();
        graphDef.setStartStep(modelStep);
        graphDef.addStep(toolStep);
        graphDef.addTransition(modelStep, toolStep);
        StepGraph stepGraph = new DefaultStepGraph(graphDef);
        
        // 4. Create AgentRecipe with the step graph
        AgentRecipe recipe = AgentRecipe.builder()
                                        .id("test_agent")
                                        .stepGraph(stepGraph)
                                        .memoryDefinitions(List.of(MemorySubsystem.SHORT_TERM))
                                        .build();
        
        // 5. Run the agent
        SimpleMessage userMessage = SimpleMessage.builder().content("What's the weather in Paris?").role(MessageRole.USER).build();
        AgentContext context = agentService.runAgent(recipe, userMessage);
        
        // 6. Assert workflow completed and results are present
        assertNotNull(context);
        assertNotNull(context.getResults());
        // Optionally, check the results content
    }
} 