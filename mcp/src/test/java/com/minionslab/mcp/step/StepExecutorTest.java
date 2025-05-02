package com.minionslab.mcp.step;

import com.minionslab.mcp.BaseExecutorTest;
import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.model.ModelCallExecutor;
import com.minionslab.mcp.model.ModelCallStatus;
import com.minionslab.mcp.tool.MCPToolCall;
import com.minionslab.mcp.tool.ToolCallExecutionContext;
import com.minionslab.mcp.tool.ToolCallStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StepExecutorTest extends BaseExecutorTest {
    
    @Mock
    private MCPStep mockStep;
    
    @Mock
    private ToolCallExecutionContext mockToolContext;
    
    @Mock
    private MCPToolCall mockToolCall1;
    
    @Mock
    private MCPToolCall mockToolCall2;
    
    @Mock
    private StepCompletionCriteria mockCompletionCriteria;
    
    private StepExecutor stepExecutor;
    
    @BeforeEach
    protected void setUp() {
        super.setUpBase();
        mockedStaticMCExecuter = mockStatic(ModelCallExecutor.class);
        mockedStaticMCExecuter.when(() -> ModelCallExecutor.forCall(any(MCPModelCall.class), any(MCPContext.class)))
                              .thenReturn(mockedModelCallExecutor);
        
        when(mockContext.getToolCallExecutionContext()).thenReturn(mockToolContext);
        doReturn(mockModelCall).when(mockStep).createInitialModelCall();
        when(mockModelCall.getToolCalls()).thenReturn(Collections.emptyList());
        when(mockCompletionCriteria.isComplete(any())).thenReturn(false);
        when(mockStep.getCompletionCriteria()).thenReturn(mockCompletionCriteria);
        
        MCPStep step = new AbstractMCPStep(mockContext, "Test") {
            @Override
            public MCPModelCall createInitialModelCall() {
                return mockModelCall;
            }
            
            @Override
            public MCPModelCall createFollowUpModelCall(MCPModelCall previousModelCall, java.util.List<com.minionslab.mcp.tool.MCPToolCall> toolCalls) {
                return mockModelCall;
            }
            
            @Override
            public StepCompletionCriteria getCompletionCriteria() {
                return mockCompletionCriteria;
            }
        };
        stepExecutor = new StepExecutor(step, mockContext, runnableExecutor);
    }
    
    @Test
    void testSuccessfulExecutionWithNoToolCalls() {
        // Setup
        doReturn(CompletableFuture.completedFuture(successModelCallResponse))
                .when(mockedModelCallExecutor).execute();
        
        when(mockModelCall.getStatus())
                .thenReturn(ModelCallStatus.PENDING).thenReturn(ModelCallStatus.COMPLETED);
        
        when(mockToolCall1.getStatus()).thenReturn(ToolCallStatus.PENDING).thenReturn(ToolCallStatus.COMPLETED);
        when(mockToolCall2.getStatus()).thenReturn(ToolCallStatus.PENDING).thenReturn(ToolCallStatus.COMPLETED);
        
        // Execute
        StepExecution result = stepExecutor.execute().join();
        
        // Verify
        assertNotNull(result);
        assertEquals(StepStatus.COMPLETED, result.getStatus());
        assertEquals(1, result.getCallGroups().size());
        assertTrue(result.getCallGroups().get(0).isComplete());
        verify(mockStep, times(1)).createInitialModelCall();
        verify(mockStep, never()).createFollowUpModelCall(any(), any());
    }
    
    @AfterEach
    void tearDownBase() {
        if (mockedStaticMCExecuter != null) {
            mockedStaticMCExecuter.close();
        }
    }
    
    @Test
    void testExecutionWithToolCalls() {
        // Setup
        when(mockModelCall.getToolCalls())
                .thenReturn(Arrays.asList(mockToolCall1, mockToolCall2));
        
        when(mockStep.createFollowUpModelCall(any(), any())).thenReturn(mockModelCall);
        
        when(mockModelCall.getToolCalls()).thenReturn(List.of(mockToolCall1));
        
        when(mockCompletionCriteria.isComplete(any())).thenReturn(true);
        
        // Configure tool calls to complete successfully
        when(mockToolCall1.getStatus()).thenReturn(ToolCallStatus.COMPLETED);
        when(mockToolCall2.getStatus()).thenReturn(ToolCallStatus.COMPLETED);
        when(mockModelCall.getStatus()).thenReturn(ModelCallStatus.COMPLETED);
        
        // Execute
        StepExecution result = stepExecutor.execute().join();
        
        // Verify
        assertNotNull(result);
        assertEquals(StepStatus.COMPLETED, result.getStatus());
        assertEquals(1, result.getCallGroups().size());
        
        CallGroup group = result.getCallGroups().get(0);
        assertEquals(2, group.getToolCalls().size());
        assertTrue(group.isComplete());
        
        verify(mockStep, times(1)).createInitialModelCall();
        verify(mockStep, times(1)).createFollowUpModelCall(any(), any());
    }
    
    @Test
    void testExecutionWithMultipleCallGroups() {
        // Setup initial call that requires follow-up
        when(mockModelCall.getToolCalls()).thenReturn(Collections.emptyList());
        
        // Configure completion criteria to require two calls
        boolean[] completionFlag = {false};
        when(mockCompletionCriteria.isComplete(any())).thenAnswer(invocation -> {
            if (!completionFlag[0]) {
                completionFlag[0] = true;
                return false;
            }
            return true;
        });
        
        // Configure follow-up call
        when(mockStep.createFollowUpModelCall(any(), any())).thenReturn(mockModelCall);
        when(mockModelCall.getStatus()).thenReturn(ModelCallStatus.COMPLETED);
        
        // Execute
        StepExecution result = stepExecutor.execute().join();
        
        // Verify
        assertNotNull(result);
        assertEquals(StepStatus.COMPLETED, result.getStatus());
        assertEquals(2, result.getCallGroups().size());
        assertTrue(result.getCallGroups().get(0).isComplete());
        assertTrue(result.getCallGroups().get(1).isComplete());
        
        verify(mockStep, times(1)).createInitialModelCall();
        verify(mockStep, times(1)).createFollowUpModelCall(any(), any());
    }
    
    @Test
    void testExecutionWithToolCallFailure() {
        // Setup
        when(mockModelCall.getToolCalls())
                .thenReturn(Arrays.asList(mockToolCall1, mockToolCall2));
        
        // Configure first tool call to fail
        when(mockToolCall1.getStatus()).thenReturn(ToolCallStatus.FAILED);
        when(mockToolCall2.getStatus()).thenReturn(ToolCallStatus.COMPLETED);
        
        // Execute
        StepExecution result = stepExecutor.execute().join();
        
        // Verify
        assertNotNull(result);
        assertEquals(StepStatus.FAILED, result.getStatus());
        assertNotNull(result.getError());
        
        CallGroup group = result.getCallGroups().get(0);
        assertFalse(group.isComplete());
        assertEquals(CallGroupStatus.EXECUTING_TOOL_CALLS, group.getStatus());
    }
    
    @Test
    void testExecutionWithModelCallFailure() {
        // Setup
        RuntimeException expectedException = new RuntimeException("Model call failed");
        when(mockedModelCallExecutor.execute())
                .thenReturn(CompletableFuture.failedFuture(expectedException));
        when(mockModelCall.getStatus())
                .thenReturn(ModelCallStatus.PENDING)
                .thenReturn(ModelCallStatus.FAILED);
        
        // Execute
        StepExecution result = stepExecutor.execute().join();
        
        // Verify
        assertNotNull(result);
        assertEquals(StepStatus.FAILED, result.getStatus());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("Model call failed"));
    }
} 