package com.minionslab.core.tool;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.tool.springai.SpringAIToolCallExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.tool.ToolCallback;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpringAIToolCallExecutorTest {
    private ToolCall toolCall;
    private AgentContext context;
    private ToolRegistry toolRegistry;
    private Executor executor;

    @BeforeEach
    void setUp() {
        toolCall = ToolCall.builder()
                .name("mockTool")
                .request(new ToolCall.ToolCallRequest("mockTool", "input", Map.of()))
                .build();
        context = mock(AgentContext.class);
        toolRegistry = mock(ToolRegistry.class);
        executor = Executors.newSingleThreadExecutor();
    }

    @Test
    void testExecuteSuccess() {
        ToolCallback callback = mock(ToolCallback.class);
        when(callback.call(anyString(), any())).thenReturn("result");
        when(toolRegistry.getTool("mockTool")).thenReturn(callback);
        SpringAIToolCallExecutor executorImpl = new SpringAIToolCallExecutor(toolCall, context, toolRegistry, executor);
        ToolCall.ToolCallResponse response = executorImpl.execute();
        assertEquals("result", response.response());
        assertNull(response.error());
        assertEquals(ToolCallStatus.COMPLETED, toolCall.getStatus());
    }

    @Test
    void testExecuteToolNotFound() {
        when(toolRegistry.getTool("mockTool")).thenReturn(null);
        SpringAIToolCallExecutor executorImpl = new SpringAIToolCallExecutor(toolCall, context, toolRegistry, executor);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> executorImpl.execute());
        assertTrue(ex.getMessage().contains("Tool not found"));
    }

    @Test
    void testExecuteCallbackThrows() {
        ToolCallback callback = mock(ToolCallback.class);
        when(callback.call(anyString(), any())).thenThrow(new RuntimeException("fail"));
        when(toolRegistry.getTool("mockTool")).thenReturn(callback);
        SpringAIToolCallExecutor executorImpl = new SpringAIToolCallExecutor(toolCall, context, toolRegistry, executor);
        try {
            executorImpl.execute();
            fail("Should throw RuntimeException");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("fail"));
        }
    }
} 