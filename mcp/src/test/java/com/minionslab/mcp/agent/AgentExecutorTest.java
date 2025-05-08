package com.minionslab.mcp.agent;

import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.memory.MCPChatMemory;
import com.minionslab.mcp.model.MCPModelCallResponse;
import com.minionslab.mcp.model.ModelCallExecutor;
import com.minionslab.mcp.model.ModelCallExecutorFactory;
import com.minionslab.mcp.step.*;
import com.minionslab.mcp.tool.ToolCallExecutor;
import com.minionslab.mcp.tool.ToolCallExecutorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AgentExecutor.
 * Ensures correct step execution, error handling, and workflow progression.
 */
class AgentExecutorTest {
    // --- Mocks ---
    @Mock
    private MCPAgent mockAgent;
    @Mock
    private AgentRecipe mockRecipe;
    @Mock
    private Step mockStep;
    @Mock
    private StepExecutor mockStepExecutor;
    @Mock
    private StepExecutor mockStepExecutor2;
    @Mock
    private StepExecution mockStepExecution;
    @Mock
    private StepExecution mockStepExecution2;
    @Mock
    private MCPChatMemory mockChatMemory;
    @Mock
    private ModelCallExecutorFactory mockModelExecutorFactory;
    @Mock
    private ToolCallExecutorFactory mockToolExecutorFactory;
    @Mock
    private MCPContext mockContext;
    @Mock
    private StepManager mockStepManager;
    @Mock
    private ToolCallExecutor mockToolExecutor;
    @Mock
    private ModelCallExecutor mockModelCallExecutor;
    @Mock
    private MCPModelCallResponse mockModelCallResponse;
    
    // --- System Under Test ---
    private AgentExecutor agentExecutor;
    
    // --- Setup ---
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockAgent.getRecipe()).thenReturn(mockRecipe);
        when(mockRecipe.getSteps()).thenReturn(List.of(mockStep));
        when(mockContext.getStepManager()).thenReturn(mockStepManager);
        when(mockStepManager.getCurrentStep()).thenReturn(mockStep).thenReturn(mockStep).thenReturn(null);
        when(mockStepManager.isWorkflowComplete()).thenReturn(false).thenReturn(true);
        when(mockContext.getMetadata()).thenReturn(Map.of("maxModelCallsPerStep", 10, "maxToolCallRetries", 2, "sequentialToolCalls", true));
        when(mockModelExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockStep.getId()).thenReturn("step1");
        when(mockToolExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockToolExecutor);
        agentExecutor = spy(
                new AgentExecutor(mockAgent, mockContext, mockModelExecutorFactory, mockToolExecutorFactory));
    }
    
    /**
     * Tests that a single step is executed and the result is correct.
     */
    @Test
    void testSingleStepExecution() {
        doReturn(mockStepExecutor).when(agentExecutor).createStepExecutor(mockStep);
        when(mockStepExecutor.execute()).thenReturn(CompletableFuture.completedFuture(mockStepExecution));
        when(mockStepManager.getPossibleNextSteps()).thenReturn(Collections.emptyList());
        AgentResult result = agentExecutor.executeSync();
        assertNotNull(result);
        assertEquals(1, result.getStepExecutions().size());
        verify(mockStepExecutor, times(1)).execute();
    }
    
    /**
     * Tests that an exception during step execution is properly handled and wrapped as AgentExecutionException.
     */
    @Test
    void testStepExecutionThrowsException() {
        when(mockStepExecutor.execute()).thenThrow(new RuntimeException("Step failed"));
        
        Exception thrown = assertThrows(Exception.class, () -> agentExecutor.executeSync());
        
        // Unwrap CompletionException if present
        Throwable cause = thrown instanceof CompletionException && thrown.getCause() != null
                                  ? thrown.getCause()
                                  : thrown;
        
        assertTrue(cause instanceof AgentExecutionException, "Exception should be AgentExecutionException");
        // Optionally, check the message or cause of the AgentExecutionException
        // assertEquals("Failed to execute step: step1", cause.getMessage());
    }
    
    /**
     * Tests that the agent executes all steps in the recipe.
     */
    @Test
    void testAgentExecutesAllSteps() {
        Step step1 = new DefaultStep("s1", "desc1", Set.of(), "prompt1");
        Step step2 = new DefaultStep("s2", "desc2", Set.of(), "prompt2");
        AgentRecipe agentRecipe = AgentRecipe.builder().steps(List.of(step1, step2)).requiredTools(List.of("tool1", "tool2")).build();
        MCPAgent agent = new DefaultMCPAgent(agentRecipe, null);
        when(mockStepManager.getCurrentStep()).thenReturn(step1).thenReturn(step2).thenReturn(null);
        when(mockStepManager.isWorkflowComplete()).thenReturn(false).thenReturn(false).thenReturn(true);
        AgentExecutor agentExecutor2 = spy(new AgentExecutor(agent, mockContext, mockModelExecutorFactory, mockToolExecutorFactory));
        doReturn(mockStepExecutor,mockStepExecutor2).when(agentExecutor2).createStepExecutor(any());
        when(mockStepExecutor.execute()).thenReturn(CompletableFuture.completedFuture(mockStepExecution));
        when(mockStepExecutor2.execute()).thenReturn(CompletableFuture.completedFuture(mockStepExecution2));
        
        when(mockStepExecution.getId()).thenReturn("step_execution_1");
        when(mockStepExecution2.getId()).thenReturn("step_execution_2");
        when(mockStepManager.getPossibleNextSteps()).thenReturn(List.of(step1)).thenReturn(List.of(step2)).thenReturn(Collections.emptyList());
        

        
        AgentResult result = agentExecutor2.executeSync();
        assertEquals(2, result.getStepExecutions().size());
//        assertTrue(result.getStepExecutions().stream().allMatch(exec -> exec.getStatus() != null));
    }
} 