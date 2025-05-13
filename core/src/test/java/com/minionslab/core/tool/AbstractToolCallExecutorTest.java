package com.minionslab.core.tool;

import com.minionslab.core.context.AgentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class AbstractToolCallExecutorTest {
    private ToolCall toolCall;
    private AgentContext context;
    private Executor executor;

    @BeforeEach
    void setUp() {
        toolCall = ToolCall.builder()
                .name("echo")
                .request(new ToolCall.ToolCallRequest("echo", "hello", null))
                .build();
        context = null; // Use a mock or minimal implementation if needed
        executor = Executors.newSingleThreadExecutor();
    }

    @Test
    void testExecuteSuccess() {
        AbstractToolCallExecutor<String> executorImpl = new AbstractToolCallExecutor<>(toolCall, context) {
            @Override
            protected String callTool(ToolCall toolCall) { return "ok"; }
            @Override
            protected ToolCall.ToolCallResponse toToolCallResponse(String rawResponse) { return new ToolCall.ToolCallResponse(rawResponse, null); }
            @Override
            protected Executor getExecutor() { return executor; }
        };
        ToolCall.ToolCallResponse response = executorImpl.execute();
        assertEquals("ok", response.response());
        assertNull(response.error());
        assertEquals(ToolCallStatus.COMPLETED, toolCall.getStatus());
    }

    @Test
    void testExecuteFailure() {
        AbstractToolCallExecutor<String> executorImpl = new AbstractToolCallExecutor<>(toolCall, context) {
            @Override
            protected String callTool(ToolCall toolCall) { throw new RuntimeException("fail"); }
            @Override
            protected ToolCall.ToolCallResponse toToolCallResponse(String rawResponse) { return new ToolCall.ToolCallResponse(rawResponse, null); }
            @Override
            protected Executor getExecutor() { return executor; }
        };
        ToolCall.ToolCallResponse response = executorImpl.execute();
        assertNull(response.response());
        assertNotNull(response.error());
        assertEquals(ToolCallStatus.FAILED, toolCall.getStatus());
    }
} 