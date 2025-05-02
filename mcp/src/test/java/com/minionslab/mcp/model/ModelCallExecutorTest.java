package com.minionslab.mcp.model;

import com.minionslab.mcp.BaseExecutorTest;
import com.minionslab.mcp.message.DefaultMCPMessage;
import com.minionslab.mcp.message.MessageRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelCallExecutorTest extends BaseExecutorTest {
    
    private ModelCallExecutor executor;
    
    @Mock(strictness = Mock.Strictness.LENIENT)
    private ChatMemoryRepository mockedChatMemoryRepository;
    
    @Mock(strictness = Mock.Strictness.LENIENT)
    private ChatModel mockedChatModel;
    
    @Mock(strictness = Mock.Strictness.LENIENT)
    private ChatResponse mockedChatResponse;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Generation mockedGeneration1;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Generation mockedGeneration2;
    
    private MCPModelCall modelCall;
    
    
    @BeforeEach
    protected void setUp() {
        super.setUpBase();
        when(mockModelContext.chatMemory()).thenReturn(mockedChatMemoryRepository);
        when(mockModelContext.chatModel()).thenReturn(mockedChatModel);
        when(mockedChatMemoryRepository.findByConversationId(anyString())).thenReturn(List.of(new AssistantMessage("This is the LLM")));
        when(mockedChatModel.call(any(Prompt.class))).thenReturn(mockedChatResponse);
        when(mockedChatResponse.getResults()).thenReturn(List.of(mockedGeneration1, mockedGeneration2));
        when(mockedGeneration1.getOutput()).thenReturn(new AssistantMessage("Genration 1"));
        when(mockedGeneration2.getOutput()).thenReturn(new AssistantMessage("Genration 2"));
        
        
        modelCall =
                new MCPModelCall(new MCPModelCall.MCPModelCallRequest(List.of(DefaultMCPMessage.builder().content("Message request 1").role(MessageRole.USER).build()), null));
        executor = new ModelCallExecutor(modelCall, mockContext, runnableExecutor);
    }
    
    @Test
    void testSuccessfulExecution() {
        // Setup
/*        doReturn(CompletableFuture.completedFuture(successModelCallResponse))
                .when(mockedModelCallExecutor).execute();*/
        MCPModelCall.MCPModelCallResponse actual =
                new MCPModelCall.MCPModelCallResponse(List.of(DefaultMCPMessage.builder().content(mockedGeneration1.getOutput().getText()).role(MessageRole.ASSISTANT).build(),
                        DefaultMCPMessage.builder().content(mockedGeneration2.getOutput().getText()).role(MessageRole.ASSISTANT).build()));
        
        when(mockModelCall.getStatus())
                .thenReturn(ModelCallStatus.PENDING)
                .thenReturn(ModelCallStatus.COMPLETED);
        
        // Execute
        MCPModelCall.MCPModelCallResponse response = executor.execute().join();
        
        // Verify
        assertNotNull(response);
        
        assertEquals(response.messages().size(), actual.messages().size());
        assertEquals(ModelCallStatus.COMPLETED, modelCall.getStatus());
        
    }
    
    @Test
    void testExecutionFailure() {
        // Setup
        RuntimeException expectedException = new RuntimeException("Model call failed");
        
        ModelCallExecutionException modelCallExecutionException = new ModelCallExecutionException("Model call failed", expectedException);
        
        when(mockModelCall.getStatus())
                .thenReturn(ModelCallStatus.PENDING)
                .thenReturn(ModelCallStatus.FAILED);
        
        when(mockedChatModel.call(any(Prompt.class))).thenThrow(expectedException);
        
        // Execute and verify
        CompletableFuture<MCPModelCall.MCPModelCallResponse> future = executor.execute();
        
        Exception thrown = assertThrows(RuntimeException.class, () -> future.join());
        assertTrue(thrown.getMessage().contains("Model call failed"));
        assertEquals(ModelCallStatus.FAILED, modelCall.getStatus());
        
    }
    
    @Test
    void testExecutionWithInvalidInitialState() {
        // Setup: use a real MCPModelCall instance with status COMPLETED
        MCPModelCall completedCall = new MCPModelCall(new MCPModelCall.MCPModelCallRequest(List.of(DefaultMCPMessage.builder().content("Message request 1").role(MessageRole.USER).build()), null));
        completedCall.setStatus(ModelCallStatus.COMPLETED);
        ModelCallExecutor completedExecutor = new ModelCallExecutor(completedCall, mockContext, runnableExecutor);
        
        // Execute and verify
        Exception thrown = assertThrows(CompletionException.class, () -> completedExecutor.execute().join());
        assertTrue(thrown.getMessage().contains("Invalid initial state"));
        assertEquals(ModelCallStatus.FAILED, completedCall.getStatus());
    }
    
} 