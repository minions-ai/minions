package com.minionslab.mcp;

import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.message.DefaultMCPMessage;
import com.minionslab.mcp.message.MessageRole;
import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.model.ModelCallExecutionContext;
import com.minionslab.mcp.model.ModelCallExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;

/**
 * Base test class for executor-related tests.
 * Provides common mocking and setup for ModelCallExecutor and StepExecutor tests.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseExecutorTest {
    
    @Mock(strictness = Mock.Strictness.LENIENT)
    protected MCPModelCall mockModelCall;
    
    @Mock
    protected MCPContext mockContext;
    
    @Mock(strictness = Mock.Strictness.LENIENT)
    protected ModelCallExecutionContext mockModelContext;
    
    @Mock(strictness = Mock.Strictness.LENIENT)
    protected MCPModelCall.MCPModelCallRequest mockModelCallRequest;
    
    protected MockedStatic<ModelCallExecutor> mockedStaticMCExecuter;
    
    @Mock(strictness = Mock.Strictness.LENIENT)
    protected ModelCallExecutor mockedModelCallExecutor;
    
    protected final Executor runnableExecutor = Runnable::run;
    
    protected final DefaultMCPMessage successResponse = DefaultMCPMessage.builder()
            .role(MessageRole.ASSISTANT)
            .content("Response from LLM")
            .build();
    
    protected final MCPModelCall.MCPModelCallResponse successModelCallResponse = 
            new MCPModelCall.MCPModelCallResponse(List.of(successResponse));
    
    @BeforeEach
    protected void setUpBase() {
        // Setup context mocks
        when(mockContext.getModelCallExecutionContext()).thenReturn(mockModelContext);
        
        // Setup model call mocks
        when(mockModelCall.getRequest()).thenReturn(mockModelCallRequest);
        when(mockModelCallRequest.messages()).thenReturn(List.of(
                DefaultMCPMessage.builder()
                        .content("You are a helpful agent")
                        .role(MessageRole.SYSTEM)
                        .build()
        ));
        
        // Setup static mocking

        when(mockedModelCallExecutor.toString()).thenReturn("MockedModelCallExecutor");
    }
    

} 