package com.minionslab.mcp.step;


import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.model.MCPModelCallResponse;
import com.minionslab.mcp.model.ModelCallExecutor;
import com.minionslab.mcp.model.ModelCallExecutorFactory;
import com.minionslab.mcp.tool.MCPToolCall;
import com.minionslab.mcp.tool.ToolCallExecutor;
import com.minionslab.mcp.tool.ToolCallExecutorFactory;
import com.minionslab.mcp.tool.ToolCallStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class StepExecutorTest {
    
    @Mock
    private Step mockStep;
    @Mock
    private MCPContext mockContext;
    @Mock
    private StepManager mockStepManager;
    @Mock
    private ModelCallExecutorFactory mockModelCallExecutorFactory;
    @Mock
    private ToolCallExecutorFactory mockToolCallExecutorFactory;
    @Mock
    private ModelCallExecutor mockModelCallExecutor;
    @Mock
    private ToolCallExecutor mockToolCallExecutor;
    @Mock
    private MCPModelCall mockModelCall;
    @Mock
    private MCPModelCallResponse mockModelCallResponse;
    @Mock
    private MCPToolCall mockToolCall;
    @Mock
    private StepExecution mockStepExecution;
    @Mock
    private Object mockToolCallResponse;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getStepManager()).thenReturn(mockStepManager);
        when(mockContext.getMetadata()).thenReturn(Map.of(
                "maxModelCallsPerStep", 3,
                "maxToolCallRetries", 1,
                "sequentialToolCalls", true
                                                         ));
        when(mockStep.getId()).thenReturn("step1");
        when(mockStep.createInitialModelCall()).thenReturn(mockModelCall);
        when(mockStep.createFollowUpModelCall(any(), any())).thenReturn(mockModelCall);
        when(mockStep.getStepExecution()).thenReturn(mockStepExecution);
        when(mockStep.getCompletionCriteria()).thenReturn(null);
    }
    
    @Test
    void testNormalStepExecutionCompletes() {
        // Model call returns instruction to complete
        Step.StepInstruction instruction = new Step.StepInstruction("step1", "result", Step.StepOutcome.COMPLETED, null, null, null, 1.0, null);
        when(mockModelCallResponse.getInstruction()).thenReturn(instruction);
        when(mockModelCallResponse.getToolCalls()).thenReturn(Collections.emptyList());
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.execute()).thenReturn(CompletableFuture.completedFuture(mockModelCallResponse));
        when(mockStepManager.getCurrentStep()).thenReturn(mockStep).thenReturn(null);
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        StepExecution result = executor.execute().join();
        
        assertNotNull(result);
        verify(mockStepExecution).complete();
    }
    
    @Test
    void testModelCallFailure() {
        
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.execute()).thenThrow(new RuntimeException("Model call failed"));
        
        StepExecutor executor = spy(new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory));
        StepExecution result = executor.execute().join();
        
        assertNotNull(result);
        assertEquals(result.getStatus(), StepStatus.FAILED);
    }
    
    @Test
    void testToolCallFailureAndRetry() {
        // Model call returns a tool call
        when(mockModelCallResponse.getInstruction()).thenReturn(null);
        when(mockModelCallResponse.getToolCalls()).thenReturn(List.of(mockToolCall));
        when(mockToolCall.getName()).thenReturn("tool1");
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.execute()).thenReturn(CompletableFuture.completedFuture(mockModelCallResponse));
        when(mockToolCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockToolCallExecutor);
        // Tool call fails first, then succeeds
        doThrow(new RuntimeException("Tool failed"))
                .when(mockToolCallExecutor).execute();
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        StepExecution result = executor.execute().join();
        
        assertNotNull(result);
        // You can add more assertions about retries, status, etc.
    }
    
    @Test
    void testToolCallFailureAndRecovery() {
        // Model call returns a tool call
        when(mockModelCallResponse.getInstruction()).thenReturn(null);
        when(mockModelCallResponse.getToolCalls()).thenReturn(List.of(mockToolCall));
        when(mockToolCall.getName()).thenReturn("tool1");
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.execute()).thenReturn(CompletableFuture.completedFuture(mockModelCallResponse));
        when(mockToolCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockToolCallExecutor);
        // Tool call fails first, then succeeds
        doThrow(new RuntimeException("Tool failed"))
                .doReturn(CompletableFuture.completedFuture(mockToolCallResponse))
                .when(mockToolCallExecutor).execute();
        
        when(mockToolCall.getStatus()).thenReturn(ToolCallStatus.FAILED).thenReturn(ToolCallStatus.COMPLETED);
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        StepExecution result = executor.execute().join();
        
        assertNotNull(result);
        // You can add more assertions about retries, status, etc.
    }
    
    @Test
    void testSequentialToolCalls() {
        // Set up two tool calls
        MCPToolCall toolCall1 = mock(MCPToolCall.class);
        MCPToolCall toolCall2 = mock(MCPToolCall.class);
        when(mockModelCallResponse.getInstruction()).thenReturn(null);
        when(mockModelCallResponse.getToolCalls()).thenReturn(List.of(toolCall1, toolCall2));
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.execute()).thenReturn(CompletableFuture.completedFuture(mockModelCallResponse));
        when(mockToolCallExecutorFactory.forProvider(any(), any(), any()))
                .thenReturn(mockToolCallExecutor);
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        StepExecution result = executor.execute().join();
        
        assertNotNull(result);
        // Add assertions for sequential execution if needed
    }
    
    @Test
    void testMaxModelCallsExceeded() {
        // Always return CONTINUE to force loop
        Step.StepInstruction instruction = new Step.StepInstruction("step1", "result", Step.StepOutcome.CONTINUE, null, null, null, 1.0, null);
        when(mockModelCallResponse.getInstruction()).thenReturn(instruction);
        when(mockModelCallResponse.getToolCalls()).thenReturn(Collections.emptyList());
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.execute()).thenReturn(CompletableFuture.completedFuture(mockModelCallResponse));
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        StepExecution result = executor.execute().join();
        
        assertNotNull(result);
        assertEquals(result.getStatus(), StepStatus.FAILED);
    }
    
    @Test
    void testNoToolCalls() {
        // Model call returns no tool calls and no instruction
        when(mockModelCallResponse.getInstruction()).thenReturn(null);
        when(mockModelCallResponse.getToolCalls()).thenReturn(Collections.emptyList());
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.execute()).thenReturn(CompletableFuture.completedFuture(mockModelCallResponse));
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        StepExecution result = executor.execute().join();
        
        assertNotNull(result);
        // Should not throw or hang
    }
}