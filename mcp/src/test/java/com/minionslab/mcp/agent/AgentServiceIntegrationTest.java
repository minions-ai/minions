package com.minionslab.mcp.agent;

import com.minionslab.mcp.config.ModelConfig;
import com.minionslab.mcp.step.Step;
import com.minionslab.mcp.step.StepExecution;
import com.minionslab.mcp.step.StepStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AgentServiceIntegrationTest {
    @Autowired
    private AgentService agentService;
    
    @MockBean
    private AgentRecipeRepository agentRecipeRepository;
    

    
    @Test
    void testRunAgentWithValidRecipe() {
        AgentRecipe recipe = mock(AgentRecipe.class);
        ModelConfig modelConfig = ModelConfig.builder()
                                             .modelId("chat")
                                             .provider("openai")
                                             .build();
        when(recipe.getModelConfig()).thenReturn(modelConfig);
        when(agentRecipeRepository.findById("recipe1")).thenReturn(recipe);
        
        MCPAgent agent = mock(MCPAgent.class);

        
        // Simulate a single step that completes successfully
        Step step = mock(Step.class);
        StepExecution stepExecution = mock(StepExecution.class);
        when(stepExecution.getStatus()).thenReturn(StepStatus.COMPLETED);
        when(stepExecution.getStep()).thenReturn(step);
        when(recipe.getSteps()).thenReturn(List.of(step));
        when(agent.getRecipe()).thenReturn(recipe);
        doAnswer(invocation -> {
            step.setStepExecution(stepExecution);
            return null;
        }).when(step).setStepExecution(any());
        
        AgentResult result = agentService.runAgent("recipe1");
        assertNotNull(result);
        assertEquals(1, result.getStepExecutions().size());
        assertEquals(StepStatus.COMPLETED, result.getStepExecutions().get(0).getStatus());
    }
    
    @Test
    void testRunAgentWithMissingRecipe() {
        when(agentRecipeRepository.findById("missing")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> agentService.runAgent("missing"));
    }
} 