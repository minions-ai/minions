package com.minionslab.core.model.impl;

import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.context.AgentContext;
import com.minionslab.core.memory.ModelMemory;
import com.minionslab.core.model.*;
import com.minionslab.core.model.springai.SpringAIModelCallExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpringAIModelCallExecutorTest {
    
    @Mock private ModelCall mockModelCall;
    @Mock private AgentContext mockContext;
    @Mock private ChatModel mockChatModel;
    @Mock private ModelMemory mockChatMemory;
    @Mock private ModelCallExecutionContext mockModelCallContext;
    @Mock private ChatResponse mockChatResponse;
    
    private SpringAIModelCallExecutor executor;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(mockContext.getChatMemory()).thenReturn(mockChatMemory);
        when(mockContext.getModelConfig()).thenReturn(mock(ModelConfig.class));
        when(mockContext.getConversationid()).thenReturn("agent1");
        when(mockContext.getAvailableTools()).thenReturn(List.of("tool1", "tool2"));
        when(mockContext.getModelConfig().getParameters()).thenReturn(Map.of());
        
        when(mockModelCall.getStatus()).thenReturn(ModelCallStatus.PENDING);
        when(mockModelCall.getRequest()).thenReturn(new ModelCall.ModelCallRequest(List.of(), Map.of()));
        
        executor = new SpringAIModelCallExecutor(mockModelCall, mockContext, mockChatModel);
    }
    
    @Test
    void testExecute_HappyPath() throws Exception {
        doNothing().when(mockChatMemory).saveChatResponse(anyString(), any(ChatResponse.class));
        doReturn(ModelCallStatus.PENDING).when(mockModelCall).getStatus();
        SpringAIModelCallExecutor spyExecutor = spy(executor);
        java.lang.reflect.Field chatModelField = SpringAIModelCallExecutor.class.getDeclaredField("chatModel");
        chatModelField.setAccessible(true);
        chatModelField.set(spyExecutor, mockChatModel);
        doReturn(mockChatResponse).when(mockChatModel).call(any(org.springframework.ai.chat.prompt.Prompt.class));
        CompletableFuture<ModelCallResponse> future = spyExecutor.executeAsync();
        ModelCallResponse response = future.join();
        assertNotNull(response);
        verify(mockChatMemory).saveChatResponse(anyString(), eq(mockChatResponse));
        verify(mockModelCall).setResponse(any(ModelCallResponse.class));
        verify(mockModelCall).setStatus(ModelCallStatus.COMPLETED);
    }
    
    @Test
    void testExecute_ModelCallThrowsException() throws Exception {
        doReturn(ModelCallStatus.PENDING).when(mockModelCall).getStatus();
        SpringAIModelCallExecutor spyExecutor = spy(executor);
        java.lang.reflect.Field chatModelField = SpringAIModelCallExecutor.class.getDeclaredField("chatModel");
        chatModelField.setAccessible(true);
        chatModelField.set(spyExecutor, mockChatModel);
        doThrow(new RuntimeException("Model error")).when(mockChatModel).call(any(org.springframework.ai.chat.prompt.Prompt.class));
        CompletableFuture<ModelCallResponse> future = spyExecutor.executeAsync();
        Exception ex = assertThrows(Exception.class, future::join);
        assertTrue(ex.getCause() instanceof ModelCallExecutionException);
        verify(mockModelCall).setStatus(ModelCallStatus.FAILED);
        verify(mockModelCall).setError(any(ModelCall.ModelCallError.class));
    }
    
    @Test
    void testBuildPrompt_UsesChatMemoryAndAvailableTools() throws Exception {
        doReturn(ModelCallStatus.PENDING).when(mockModelCall).getStatus();
        when(mockChatMemory.findByConversationId(anyString())).thenReturn(List.of());
        java.lang.reflect.Method buildPromptMethod = SpringAIModelCallExecutor.class.getDeclaredMethod("buildMCPPrompt");
        buildPromptMethod.setAccessible(true);
        Prompt prompt = (Prompt) buildPromptMethod.invoke(executor);
        assertNotNull(prompt);
        assertTrue(prompt.getOptions().containsKey("availableTools"));
        assertTrue(prompt.getOptions().containsKey("chatOptions"));
    }
    
    @Test
    void testBuildPrompt_InvalidStateThrows() throws Exception {
        when(mockModelCall.getStatus()).thenReturn(ModelCallStatus.COMPLETED);
        java.lang.reflect.Method buildPromptMethod = SpringAIModelCallExecutor.class.getDeclaredMethod("buildMCPPrompt");
        buildPromptMethod.setAccessible(true);
        assertThrows(Exception.class, () -> buildPromptMethod.invoke(executor));
    }
    
    @Test
    void testExecute_UpdatesModelCallStatus() throws Exception {
        doNothing().when(mockChatMemory).saveChatResponse(anyString(), any(ChatResponse.class));
        doReturn(ModelCallStatus.PENDING).when(mockModelCall).getStatus();
        SpringAIModelCallExecutor spyExecutor = spy(executor);
        java.lang.reflect.Field chatModelField = SpringAIModelCallExecutor.class.getDeclaredField("chatModel");
        chatModelField.setAccessible(true);
        chatModelField.set(spyExecutor, mockChatModel);
        doReturn(mockChatResponse).when(mockChatModel).call(any(org.springframework.ai.chat.prompt.Prompt.class));
        spyExecutor.executeAsync().join();
        verify(mockModelCall).setStatus(ModelCallStatus.EXECUTING);
        verify(mockModelCall).setStatus(ModelCallStatus.COMPLETED);
    }
}