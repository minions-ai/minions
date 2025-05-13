package com.minionslab.core.agent;

import com.minionslab.core.memory.ModelMemory;
import com.minionslab.core.memory.ModelMemoryFactory;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.model.ModelCallExecutorFactory;
import com.minionslab.core.service.ChatModelService;
import com.minionslab.core.step.DefaultStep;
import com.minionslab.core.step.DefaultStepGraph;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.StepExecution;
import com.minionslab.core.tool.ToolCallExecutorFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TestGoalSettingRecipe {
    @Autowired
    private AgentService agentService;
    
    @Test
    void testRecipeConstruction() {
        String userMessage = "I want to book a flight to Paris.";
        AgentRecipe recipe = create(userMessage);
        assertNotNull(recipe);
        List<Step> steps = recipe.getStepGraph().getSteps();
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        
        assertEquals("goal_identification", step.getId());
        assertEquals(userMessage, step.createInitialModelCall().getRequest().messages().get(0).getContent());
    }
    
    /**
     * Creates a minimal AgentRecipe for goal setting from a user message.
     */
    public static AgentRecipe create(String userMessage) {
        DefaultMessage goalMessage = DefaultMessage.builder().role(MessageRole.GOAL).content("Identify the agent's goal from the user message.").build();
        DefaultMessage user = DefaultMessage.builder().role(MessageRole.USER).content(userMessage).build();
        
        List<Step> steps = List.of(new DefaultStep("goal_identification", new MessageBundle(List.of(goalMessage, user)), Set.of()));
        return AgentRecipe.builder().id("goal-setting-agent").stepGraph(new DefaultStepGraph(steps, Map.of("doal_definition", List.of()))).build();
    }
    
    @Test
    void testAgentServiceWithGoalSettingRecipe() {
        // Arrange
        String userMessage = "I want to book a flight to Paris.";
        AgentRecipe recipe = create(userMessage);
        
        // Mock dependencies for AgentService
        AgentRecipeRepository mockRepo = Mockito.mock(AgentRecipeRepository.class);
        ChatModelService mockChatModelService = Mockito.mock(ChatModelService.class);
        ModelMemoryFactory mockMemoryFactory = Mockito.mock(ModelMemoryFactory.class);
        ModelCallExecutorFactory mockModelCallExecutorFactory = Mockito.mock(ModelCallExecutorFactory.class);
        ToolCallExecutorFactory mockToolCallExecutorFactory = Mockito.mock(ToolCallExecutorFactory.class);
        ModelMemory mockChatMemory = Mockito.mock(ModelMemory.class);
        
        Mockito.when(mockMemoryFactory.create(Mockito.any())).thenReturn(mockChatMemory);
        
        AgentService agentService = new AgentService(mockRepo, mockChatModelService, mockMemoryFactory, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        
        // Mock AgentExecutor and result
        // (If AgentService uses real execution, you may need to mock deeper or use a test config)
        // For now, just check that the service can run the recipe and returns a non-null result
        AgentResult result = agentService.runAgent(recipe, userMessage);
        assertNotNull(result);
        assertFalse(result.getStepExecutions().isEmpty());
        StepExecution exec = result.getStepExecutions().get(0);
        assertNotNull(exec);
    }
    
    @Test
    void integrationTestAgentServiceWithGoalSettingRecipe() {
        // This test assumes you have real, working implementations of all dependencies on the classpath.
        // It will run a real agent workflow end-to-end.
        String userMessage = "I want to book a flight to Paris.";
        AgentRecipe recipe = create(userMessage);
        
        
        AgentResult result = agentService.runAgent(recipe, userMessage);
        assertNotNull(result);
        assertFalse(result.getStepExecutions().isEmpty());
        StepExecution exec = result.getStepExecutions().get(0);
        assertNotNull(exec);
        // Optionally print or assert more about the result
        System.out.println("Integration test result: " + exec);
    }
}
