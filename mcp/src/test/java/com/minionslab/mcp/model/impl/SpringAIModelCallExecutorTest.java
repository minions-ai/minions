package com.minionslab.mcp.model.springai;

import com.minionslab.mcp.config.ModelConfig;
import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.memory.MCPChatMemory;
import com.minionslab.mcp.message.DefaultMCPMessage;
import com.minionslab.mcp.message.MessageRole;
import com.minionslab.mcp.model.*;
import com.minionslab.mcp.step.Step;
import com.minionslab.mcp.step.StepManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpringAIModelCallExecutorTest {
    
    @Mock private MCPModelCall mockModelCall;
    @Mock private MCPContext mockContext;
    @Mock private ChatModel mockChatModel;
    @Mock private MCPChatMemory mockChatMemory;
    @Mock private ModelCallExecutionContext mockModelCallContext;
    @Mock private ChatResponse mockChatResponse;
    @Mock private Step mockStep;
    
    private SpringAIModelCallExecutor executor;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(mockContext.getChatMemory()).thenReturn(mockChatMemory);
        when(mockContext.getStepManager()).thenReturn(mock(StepManager.class));
        when(mockContext.getModelConfig()).thenReturn(mock(ModelConfig.class));
        when(mockContext.getAgentId()).thenReturn("agent1");
        when(mockContext.getAvailableTools()).thenReturn(List.of("tool1", "tool2"));
        when(mockContext.getModelConfig().getParameters()).thenReturn(Map.of());
        
        when(mockModelCall.getStatus()).thenReturn(ModelCallStatus.PENDING);
        when(mockModelCall.getRequest()).thenReturn(new MCPModelCall.MCPModelCallRequest(List.of(), Map.of()));
        
        executor = new SpringAIModelCallExecutor(mockModelCall, mockContext, mockChatModel);
    }
    
    @Test
    void testExecute_HappyPath() {
        // Arrange
        when(mockContext.getStepManager().getPossibleNextSteps()).thenReturn(List.of());
        doNothing().when(mockChatMemory).saveChatResponse(anyString(), any(ChatResponse.class));
        doReturn(ModelCallStatus.PENDING).when(mockModelCall).getStatus();
        
        // Mock callModel to return a ChatResponse
        SpringAIModelCallExecutor spyExecutor = spy(executor);
        doReturn(mockChatResponse).when(spyExecutor).callModel(any(Prompt.class));
        
        // Act
        CompletableFuture<MCPModelCallResponse> future = spyExecutor.execute();
        MCPModelCallResponse response = future.join();
        
        // Assert
        assertNotNull(response);
        verify(mockChatMemory).saveChatResponse(anyString(), eq(mockChatResponse));
        verify(mockModelCall).setResponse(any(MCPModelCallResponse.class));
        verify(mockModelCall).setStatus(ModelCallStatus.COMPLETED);
    }
    
    @Test
    void testExecute_ModelCallThrowsException() {
        // Arrange
        when(mockContext.getStepManager().getPossibleNextSteps()).thenReturn(List.of());
        doReturn(ModelCallStatus.PENDING).when(mockModelCall).getStatus();
        
        SpringAIModelCallExecutor spyExecutor = spy(executor);
        doThrow(new RuntimeException("Model error")).when(spyExecutor).callModel(any(Prompt.class));
        
        // Act & Assert
        CompletableFuture<MCPModelCallResponse> future = spyExecutor.execute();
        Exception ex = assertThrows(Exception.class, future::join);
        assertTrue(ex.getCause() instanceof ModelCallExecutionException);
        verify(mockModelCall).setStatus(ModelCallStatus.FAILED);
        verify(mockModelCall).setError(any(MCPModelCall.MCPModelCallError.class));
    }
    
    @Test
    void testBuildMCPPrompt_WithNextSteps() {
        // Arrange
        Step step1 = mock(Step.class);
        when(step1.getId()).thenReturn("s1");
        when(step1.getDescription()).thenReturn("desc1");
        Step step2 = mock(Step.class);
        when(step2.getId()).thenReturn("s2");
        when(step2.getDescription()).thenReturn("desc2");
        when(mockContext.getStepManager().getPossibleNextSteps()).thenReturn(List.of(step1, step2));
        doReturn(ModelCallStatus.PENDING).when(mockModelCall).getStatus();
        
        // Act
        MCPPrompt prompt = executor.buildMCPPrompt();
        
        // Assert
        assertNotNull(prompt);
        List<?> messages = prompt.getMessages();
        assertTrue(messages.stream().anyMatch(m -> ((DefaultMCPMessage)m).getRole() == MessageRole.SYSTEM));
        assertTrue(prompt.getOptions().containsKey("availableTools"));
    }
    
    @Test
    void testBuildMCPPrompt_InvalidStateThrows() {
        // Arrange
        when(mockModelCall.getStatus()).thenReturn(ModelCallStatus.COMPLETED);
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> executor.buildMCPPrompt());
    }
    
    @Test
    void testExecute_UpdatesModelCallStatus() {
        // Arrange
        when(mockContext.getStepManager().getPossibleNextSteps()).thenReturn(List.of());
        doNothing().when(mockChatMemory).saveChatResponse(anyString(), any(ChatResponse.class));
        doReturn(ModelCallStatus.PENDING).when(mockModelCall).getStatus();
        
        SpringAIModelCallExecutor spyExecutor = spy(executor);
        doReturn(mockChatResponse).when(spyExecutor).callModel(any(Prompt.class));
        
        // Act
        spyExecutor.execute().join();
        
        // Assert
        verify(mockModelCall).setStatus(ModelCallStatus.EXECUTING);
        verify(mockModelCall).setStatus(ModelCallStatus.COMPLETED);
    }
}