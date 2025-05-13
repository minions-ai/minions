package com.minionslab.core.agent;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.memory.ModelMemory;
import com.minionslab.core.model.ModelCallResponse;
import com.minionslab.core.model.ModelCallExecutor;
import com.minionslab.core.model.ModelCallExecutorFactory;
import com.minionslab.core.step.*;
import com.minionslab.core.tool.ToolCallExecutor;
import com.minionslab.core.tool.ToolCallExecutorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.step.Step;

/**
 * Unit tests for AgentExecutor.
 * Ensures correct step execution, error handling, and workflow progression.
 */
class AgentExecutorTest {
    // --- Mocks ---
    @Mock
    private Agent mockAgent;
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
    private ModelMemory mockChatMemory;
    @Mock
    private ModelCallExecutorFactory mockModelExecutorFactory;
    @Mock
    private ToolCallExecutorFactory mockToolExecutorFactory;
    @Mock
    private AgentContext mockContext;
    @Mock
    private StepManager mockStepManager;
    @Mock
    private ToolCallExecutor mockToolExecutor;
    @Mock
    private ModelCallExecutor mockModelCallExecutor;
    @Mock
    private ModelCallResponse mockModelCallResponse;
    
    // --- System Under Test ---
    private AgentExecutor agentExecutor;
    
    // --- Setup ---
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getStepManager()).thenReturn(mockStepManager);
        when(mockStepManager.getCurrentStep()).thenReturn(mockStep).thenReturn(mockStep).thenReturn(null);
        when(mockStepManager.isWorkflowComplete()).thenReturn(false).thenReturn(true);
        when(mockContext.getMetadata()).thenReturn(Map.of("maxModelCallsPerStep", 10, "maxToolCallRetries", 2, "sequentialToolCalls", true));
        when(mockModelExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockStep.getId()).thenReturn("step1");
        when(mockToolExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockToolExecutor);
        agentExecutor = spy(
                new AgentExecutor(mockContext, mockModelExecutorFactory, mockToolExecutorFactory));
    }
    
    /**
     * Tests that a single step is executed and the result is correct.
     */
    @Test
    void testSingleStepExecution() {
        doReturn(mockStepExecutor).when(agentExecutor).createStepExecutor(mockStep);
        when(mockStepExecutor.executeAsync()).thenReturn(CompletableFuture.completedFuture(mockStepExecution));
        AgentResult result = agentExecutor.execute();
        assertNotNull(result);
        assertEquals(1, result.getStepExecutions().size());
        verify(mockStepExecutor, times(1)).executeAsync();
    }
    
    /**
     * Tests that an exception during step execution is properly handled and wrapped as AgentExecutionException.
     */
    @Test
    void testStepExecutionThrowsException() {
        when(mockStepExecutor.executeAsync()).thenThrow(new RuntimeException("Step failed"));
        
        Exception thrown = assertThrows(Exception.class, () -> agentExecutor.execute());
        
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
        MessageBundle bundle1 = new MessageBundle();
        bundle1.addMessage(DefaultMessage.builder()
                .role(MessageRole.SYSTEM)
                .scope(MessageScope.STEP)
                .content("desc1")
                .build());
        MessageBundle bundle2 = new MessageBundle();
        bundle2.addMessage(DefaultMessage.builder()
                .role(MessageRole.SYSTEM)
                .scope(MessageScope.STEP)
                .content("desc2")
                .build());
        Step step1 = new DefaultStep("s1", bundle1, Set.of());
        Step step2 = new DefaultStep("s2", bundle2, Set.of());
        when(mockStepManager.getCurrentStep()).thenReturn(step1).thenReturn(step2).thenReturn(null);
        when(mockStepManager.isWorkflowComplete()).thenReturn(false).thenReturn(false).thenReturn(true);
        AgentExecutor agentExecutor2 = spy(new AgentExecutor(mockContext, mockModelExecutorFactory, mockToolExecutorFactory));
        doReturn(mockStepExecutor,mockStepExecutor2).when(agentExecutor2).createStepExecutor(any());
        when(mockStepExecutor.executeAsync()).thenReturn(CompletableFuture.completedFuture(mockStepExecution));
        when(mockStepExecutor2.executeAsync()).thenReturn(CompletableFuture.completedFuture(mockStepExecution2));
        when(mockStepExecution.getId()).thenReturn("step_execution_1");
        when(mockStepExecution2.getId()).thenReturn("step_execution_2");
        AgentResult result = agentExecutor2.execute();
        assertEquals(2, result.getStepExecutions().size());
    }
} 