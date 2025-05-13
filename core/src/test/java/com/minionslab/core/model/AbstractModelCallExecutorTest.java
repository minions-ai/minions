package com.minionslab.core.model;

import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.context.AgentContext;
import com.minionslab.core.memory.ModelMemory;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractModelCallExecutorTest {
    // Minimal Generation class for mocking
    
    

    private ModelCall modelCall;
    
    @Mock(strictness = Mock.Strictness.LENIENT)
    private ChatResponse mockChatResponse;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Generation mockGeneration;
    
    @Mock(strictness = Mock.Strictness.LENIENT)
    private AssistantMessage mockMessage;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private AgentContext context;
    private Executor executor;
    private AbstractModelCallExecutor<ChatResponse> executorImpl;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private ModelMemory mockChatMemory;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private ModelConfig mockModelConfig;
    
    @BeforeEach
    void setUp() {
        Message message1 = DefaultMessage.builder().content("SystemMessage").role(MessageRole.SYSTEM).build();
        Message message2 = DefaultMessage.builder().content("SystemMessage 2").role(MessageRole.SYSTEM).build();
        Message message3 = DefaultMessage.builder().content("GoalMessage").role(MessageRole.GOAL).build();
        modelCall = new ModelCall(new ModelCall.ModelCallRequest(List.of(message1, message2, message3), Map.of()));
        executor = Executors.newSingleThreadExecutor();
        when(mockGeneration.getOutput()).thenReturn(mockMessage);
        when(mockChatResponse.getResults()).thenReturn(Collections.singletonList(mockGeneration));
        when(context.getChatMemory()).thenReturn(mockChatMemory);
        when(context.getConversationid()).thenReturn("agent1");
        when(context.getModelConfig()).thenReturn(mockModelConfig);
        executorImpl = new AbstractModelCallExecutor<ChatResponse>(modelCall, context) {
            @Override
            protected ChatResponse callModel(Prompt prompt) {
                return mockChatResponse;
            }
            
            @Override
            protected ModelCallResponse toMCPModelCallResponse(ChatResponse rawResponse) {
                return new ModelCallResponse(rawResponse);
            }
            
            
            @Override
            protected Executor getExecutor() {
                return executor;
            }
        };
    }
    
    @Test
    void testExecuteSuccess() {
        
        
        ModelCallResponse response = executorImpl.executeAsync().join();
        assertEquals(1, response.getMessages().size());
        assertEquals(ModelCallStatus.COMPLETED, modelCall.getStatus());
    }
    
    @Test
    void testExecuteFailure() {
        AbstractModelCallExecutor<ChatResponse> executorImpl = new AbstractModelCallExecutor<>(modelCall, context) {
            @Override
            protected ChatResponse callModel(Prompt prompt) {
                throw new RuntimeException("fail");
            }
            
            @Override
            protected ModelCallResponse toMCPModelCallResponse(ChatResponse rawResponse) {
                return new ModelCallResponse(rawResponse);
            }
            
            @Override
            protected Executor getExecutor() {
                return executor;
            }
        };
        try {
            executorImpl.executeAsync().join();
            fail("Should throw ModelCallExecutionException");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof ModelCallExecutionException);
            assertEquals(ModelCallStatus.FAILED, modelCall.getStatus());
        }
    }
    
    @Test
    void testBuildPrompt() {
        
        Prompt prompt = executorImpl.buildPrompt();
        assertNotNull(prompt);
        assertEquals(3, prompt.getMessages().size());

    }
    
    @Test
    void testFinalizeModelCall() {
        AbstractModelCallExecutor<ChatResponse> executorImpl = new AbstractModelCallExecutor<>(modelCall, context) {
            @Override
            protected ChatResponse callModel(Prompt prompt) {
                return null;
            }
            
            @Override
            protected ModelCallResponse toMCPModelCallResponse(ChatResponse rawResponse) {
                return null;
            }
            
            @Override
            protected Executor getExecutor() {
                return executor;
            }
        };
        ModelCallResponse response = mock(ModelCallResponse.class);
        executorImpl.finalizeModelCall(response);
        assertEquals(ModelCallStatus.COMPLETED, modelCall.getStatus());
        assertEquals(response, modelCall.getResponse());
    }
    
    @Test
    void testHandleModelCallError() {
        AbstractModelCallExecutor<ChatResponse> executorImpl = new AbstractModelCallExecutor<>(modelCall, context) {
            @Override
            protected ChatResponse callModel(Prompt prompt) {
                return null;
            }
            
            @Override
            protected ModelCallResponse toMCPModelCallResponse(ChatResponse rawResponse) {
                return null;
            }
            
            @Override
            protected Executor getExecutor() {
                return executor;
            }
        };
        Exception ex = new RuntimeException("fail");
        executorImpl.handleModelCallError(ex);
        assertEquals(ModelCallStatus.FAILED, modelCall.getStatus());
        assertNotNull(modelCall.getError());
        assertTrue(modelCall.getError().error().getContent().contains("fail"));
    }
} 