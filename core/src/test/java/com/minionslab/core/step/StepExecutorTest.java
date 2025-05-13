package com.minionslab.core.step;


import com.minionslab.core.context.AgentContext;
import com.minionslab.core.memory.ModelMemory;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.model.*;
import com.minionslab.core.tool.ToolCall;
import com.minionslab.core.tool.ToolCallExecutor;
import com.minionslab.core.tool.ToolCallExecutorFactory;
import com.minionslab.core.tool.ToolCallStatus;
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
    
    
    private DefaultStep mockStep;
    @Mock
    private AgentContext mockContext;
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
    private ModelCall mockModelCall;
    @Mock
    private ModelCallResponse mockModelCallResponse;
    @Mock
    private ToolCall mockToolCall;
    @Mock
    private StepExecution mockStepExecution;
    @Mock
    private ToolCall.ToolCallResponse mockToolCallResponse;
    @Mock
    private ModelMemory mockChatMemory;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Message message1 = DefaultMessage.builder().content("SystemMessage").role(MessageRole.SYSTEM).build();
        Message message2 = DefaultMessage.builder().content("SystemMessage 2").role(MessageRole.SYSTEM).build();
        Message message3 = DefaultMessage.builder().content("GoalMessage").role(MessageRole.GOAL).build();
        MessageBundle bundle = new MessageBundle(List.of(message1, message2, message3));
        mockStep = spy(new DefaultStep("step1", bundle, Collections.emptySet()));
        
        
        when(mockContext.getStepManager()).thenReturn(mockStepManager);
        when(mockContext.getChatMemory()).thenReturn(mockChatMemory);
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
        
        when(mockStep.getMessageBundle()).thenReturn(bundle);
    }
    
    @Test
    void testNormalStepExecutionCompletes() {
        // Model call returns no tool calls, simulating a mockStep that completes immediately
        when(mockModelCallResponse.getToolCalls()).thenReturn(Collections.emptyList());
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.executeAsync()).thenReturn(CompletableFuture.completedFuture(mockModelCallResponse));
        when(mockStepManager.getCurrentStep()).thenReturn(mockStep).thenReturn(null);
        doAnswer(invocation -> {
            when(mockStepExecution.getStatus()).thenReturn(StepStatus.COMPLETED);
            return null;
        }).when(mockStepExecution).complete();
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        StepExecution result = executor.executeAsync().join();
        
        assertNotNull(result);
        // Simulate completion
        verify(mockStepExecution).complete();
    }
    
    @Test
    void testModelCallFailure() {
        
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.executeAsync()).thenThrow(new RuntimeException("Model call failed"));
        
        StepExecutor executor = spy(new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory));
        StepExecution result = executor.executeAsync().join();
        
        assertNotNull(result);
        assertEquals(result.getStatus(), StepStatus.FAILED);
    }
    
    @Test
    void testToolCallFailureAndRetry() {
        // Set up
        when(mockModelCallResponse.getToolCalls()).thenReturn(List.of(mockToolCall));
        when(mockToolCall.getName()).thenReturn("tool1");
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.execute()).thenReturn(mockModelCallResponse);
        when(mockToolCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockToolCallExecutor);
        // Tool call fails first, then succeeds
        when(mockToolCallExecutor.execute()).thenThrow(new RuntimeException("Tool failed"));
//        when(mockToolCall.getStatus()).thenReturn(ToolCallStatus.FAILED).thenReturn(ToolCallStatus.COMPLETED);
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        
        StepExecution result = executor.execute();
        
        assertNotNull(result);
        // You can add more assertions about retries, status, etc.
    }
    
    @Test
    void testToolCallFailureAndRecovery() {
        // Set up
        when(mockModelCallResponse.getToolCalls()).thenReturn(List.of(mockToolCall));
        when(mockToolCall.getName()).thenReturn("tool1");
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.execute()).thenReturn(mockModelCallResponse);
        when(mockToolCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockToolCallExecutor);
        // Tool call fails first, then succeeds
        when(mockToolCallExecutor.execute()).thenThrow(new RuntimeException("Tool failed")).thenReturn(mockToolCallResponse);
//        when(mockToolCall.getStatus()).thenReturn(ToolCallStatus.FAILED).thenReturn(ToolCallStatus.COMPLETED);
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        
        StepExecution result = executor.execute();
        
        assertNotNull(result);
        assertEquals(result.getStatus(), StepStatus.COMPLETED);
        
        // You can add more assertions about retries, status, etc.
    }
    
    @Test
    void testSequentialToolCalls() {
        // Set up two tool calls
        ToolCall toolCall1 = mock(ToolCall.class);
        ToolCall toolCall2 = mock(ToolCall.class);
        when(mockModelCallResponse.getToolCalls()).thenReturn(List.of(toolCall1, toolCall2));
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.executeAsync()).thenReturn(CompletableFuture.completedFuture(mockModelCallResponse));
        when(mockToolCallExecutorFactory.forProvider(any(), any(), any()))
                .thenReturn(mockToolCallExecutor);
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        StepExecution result = executor.executeAsync().join();
        
        assertNotNull(result);
        // Add assertions for sequential execution if needed
    }
    
    @Test
    void testMaxModelCallsExceeded() {
        // Always return no tool calls to force loop until maxModelCalls is exceeded
        when(mockModelCallResponse.getToolCalls()).thenReturn(Collections.emptyList());
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.executeAsync()).thenReturn(CompletableFuture.completedFuture(mockModelCallResponse));
        // Simulate that the mockStep never completes
        when(mockStepExecution.getStatus()).thenReturn(StepStatus.IN_PROGRESS);
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        StepExecution result = executor.executeAsync().join();
        
        assertNotNull(result);
        // Should fail due to max model calls exceeded
        // The StepExecutor will set status to FAILED
        // (simulate this in your StepExecution mock if needed)
    }
    
    @Test
    void testNoToolCalls() {
        // Model call returns no tool calls
        when(mockModelCallResponse.getToolCalls()).thenReturn(Collections.emptyList());
        when(mockModelCallExecutorFactory.forProvider(any(), any(), any())).thenReturn(mockModelCallExecutor);
        when(mockModelCallExecutor.executeAsync()).thenReturn(CompletableFuture.completedFuture(mockModelCallResponse));
        
        StepExecutor executor = new StepExecutor(mockStep, mockContext, mockModelCallExecutorFactory, mockToolCallExecutorFactory);
        StepExecution result = executor.executeAsync().join();
        
        assertNotNull(result);
        // Should not throw or hang
    }
}