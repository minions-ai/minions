package com.minionslab.mcp.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.mcp.BaseExecutorTest;
import com.minionslab.mcp.config.ModelConfig;
import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.model.ModelCallStatus;
import com.minionslab.mcp.step.MCPStep;
import com.minionslab.mcp.step.StepExecution;
import com.minionslab.mcp.step.StepExecutor;
import com.minionslab.mcp.step.StepStatus;
import com.minionslab.mcp.tool.MCPToolCall;
import com.minionslab.mcp.tool.ToolCallStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgentExecutorTest extends BaseExecutorTest {

    @Mock
    private MCPAgent mockAgent;

    @Mock
    private AgentRecipe mockRecipe;

    @Mock
    private MCPStep mockStep1;

    @Mock
    private MCPStep mockStep2;

    @Mock
    private MCPStep mockStep3;

    @Mock
    private ModelConfig mockModelConfig;

    private AgentExecutor agentExecutor;
    private List<MCPStep> steps;

    @BeforeEach
    void setUp() {
        super.setUpBase();
        
        // Initialize steps
        steps = Arrays.asList(mockStep1, mockStep2, mockStep3);
        
        // Setup mock IDs
        when(mockStep1.getId()).thenReturn("step1");
        when(mockStep2.getId()).thenReturn("step2");
        when(mockStep3.getId()).thenReturn("step3");
        
        // Setup agent and recipe
        when(mockAgent.getRecipe()).thenReturn(mockRecipe);
        when(mockAgent.getAgentId()).thenReturn("test-agent");
        when(mockRecipe.getModelConfig()).thenReturn(mockModelConfig);
        when(mockRecipe.getSteps()).thenReturn(steps);
        
        // Create executor
        agentExecutor = new AgentExecutor(mockAgent);
    }

    @Test
    void testSequentialStepExecution() {
        // Setup mock step executions
        setupSuccessfulStepExecution(mockStep1);
        setupSuccessfulStepExecution(mockStep2);
        setupSuccessfulStepExecution(mockStep3);

        // Execute
        AgentResult result = agentExecutor.executeSync();

        // Verify
        assertNotNull(result);
        assertEquals(3, result.getStepExecutions().size());
        verify(mockStep1, times(1)).createInitialModelCall();
        verify(mockStep2, times(1)).createInitialModelCall();
        verify(mockStep3, times(1)).createInitialModelCall();
    }

    @Test
    void testLLMSuggestedStepExecution() {
        // Setup mock step executions with LLM suggestion
        setupStepExecutionWithNextStepSuggestion(mockStep1, "step3");
        setupSuccessfulStepExecution(mockStep2);
        setupSuccessfulStepExecution(mockStep3);

        // Execute
        AgentResult result = agentExecutor.executeSync();

        // Verify
        assertNotNull(result);
        assertEquals(3, result.getStepExecutions().size());
        
        // Verify execution order (step1 -> step3 -> step2)
        List<StepExecution> executions = result.getStepExecutions();
        assertEquals("step1", executions.get(0).getStep().getId());
        assertEquals("step3", executions.get(1).getStep().getId());
        assertEquals("step2", executions.get(2).getStep().getId());
    }

    @Test
    void testInvalidStepSuggestion() {
        // Setup mock step executions with invalid step suggestion
        setupStepExecutionWithNextStepSuggestion(mockStep1, "invalid-step");
        setupSuccessfulStepExecution(mockStep2);
        setupSuccessfulStepExecution(mockStep3);

        // Execute
        AgentResult result = agentExecutor.executeSync();

        // Verify normal sequential execution after invalid suggestion
        assertNotNull(result);
        assertEquals(3, result.getStepExecutions().size());
        
        List<StepExecution> executions = result.getStepExecutions();
        assertEquals("step1", executions.get(0).getStep().getId());
        assertEquals("step2", executions.get(1).getStep().getId());
        assertEquals("step3", executions.get(2).getStep().getId());
    }

    @Test
    void testStepExecutionFailure() {
        // Setup successful first step
        setupSuccessfulStepExecution(mockStep1);
        
        // Setup failing second step
        when(mockStep2.createInitialModelCall()).thenThrow(new RuntimeException("Step execution failed"));
        
        // Execute and verify exception is thrown
        AgentExecutionException exception = assertThrows(
            AgentExecutionException.class,
            () -> agentExecutor.executeSync()
        );
        
        assertTrue(exception.getMessage().contains("Failed to execute step"));
    }

    @Test
    void testInfiniteLoopPrevention() {
        // Setup step that keeps suggesting itself
        setupStepExecutionWithNextStepSuggestion(mockStep1, "step1");
        
        // Execute and verify exception is thrown
        AgentExecutionException exception = assertThrows(
            AgentExecutionException.class,
            () -> agentExecutor.executeSync()
        );
        
        assertTrue(exception.getMessage().contains("infinite loop detected"));
    }

    @Test
    void testEmptyStepList() {
        // Setup agent with no steps
        when(mockRecipe.getSteps()).thenReturn(Collections.emptyList());
        agentExecutor = new AgentExecutor(mockAgent);

        // Execute
        AgentResult result = agentExecutor.executeSync();

        // Verify
        assertNotNull(result);
        assertTrue(result.getStepExecutions().isEmpty());
    }

    private void setupSuccessfulStepExecution(MCPStep step) {
        MCPModelCall mockModelCall = mock(MCPModelCall.class);
        when(step.createInitialModelCall()).thenReturn(mockModelCall);
        when(mockModelCall.getToolCalls()).thenReturn(Collections.emptyList());
        when(mockModelCall.getStatus()).thenReturn(ModelCallStatus.COMPLETED);
        
        StepExecution mockStepExecution = mock(StepExecution.class);
        when(mockStepExecution.getStatus()).thenReturn(StepStatus.COMPLETED);
        when(mockStepExecution.getStep()).thenReturn(step);
        when(mockStepExecution.getModelCalls()).thenReturn(Collections.singletonList(mockModelCall));
        
        doAnswer(invocation -> {
            StepExecution execution = invocation.getArgument(0);
            return null;
        }).when(step).setStepExecution(any());
    }

    private void setupStepExecutionWithNextStepSuggestion(MCPStep step, String nextStepId) {
        MCPModelCall mockModelCall = mock(MCPModelCall.class);
        when(step.createInitialModelCall()).thenReturn(mockModelCall);
        
        // Create a tool call with next step suggestion
        MCPToolCall mockToolCall = mock(MCPToolCall.class);
        when(mockToolCall.getName()).thenReturn("step_completed");
        when(mockToolCall.getRequest()).thenReturn(
            new MCPToolCall.MCPToolCallRequest(
                "step_completed",
                createStepCompletionParameters(nextStepId),
                "Step completion"
            )
        );
        when(mockToolCall.getStatus()).thenReturn(ToolCallStatus.COMPLETED);
        
        when(mockModelCall.getToolCalls()).thenReturn(Collections.singletonList(mockToolCall));
        when(mockModelCall.getStatus()).thenReturn(ModelCallStatus.COMPLETED);
        
        StepExecution mockStepExecution = mock(StepExecution.class);
        when(mockStepExecution.getStatus()).thenReturn(StepStatus.COMPLETED);
        when(mockStepExecution.getStep()).thenReturn(step);
        when(mockStepExecution.getModelCalls()).thenReturn(Collections.singletonList(mockModelCall));
        
        doAnswer(invocation -> {
            StepExecution execution = invocation.getArgument(0);
            return null;
        }).when(step).setStepExecution(any());
    }

    private String createStepCompletionParameters(String nextStepId) {
        Map<String, Object> params = new HashMap<>();
        params.put("nextStepSuggestion", nextStepId);
        try {
            return new ObjectMapper().writeValueAsString(params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create step completion parameters", e);
        }
    }
} 