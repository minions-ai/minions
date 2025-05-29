package com.minionslab.core.step.processor;

import com.minionslab.core.service.ToolCallService;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.tool.ToolCall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ToolCallStepProcessorTest {
    ToolCallService toolCallService;
    ToolCallStepProcessor processor;
    StepContext context;
    @BeforeEach
    void setUp() {
        toolCallService = mock(ToolCallService.class);
        processor = new ToolCallStepProcessor(toolCallService);
        context = mock(StepContext.class);
    }
    @Test
    void testAcceptsWithToolCalls() {
        ToolCall toolCall = mock(ToolCall.class);
        when(context.getToolCalls()).thenReturn(List.of(toolCall));
        when(context.unfinishedToolCalls()).thenReturn(List.of(toolCall));
        assertTrue(processor.accepts(context));
        when(context.getToolCalls()).thenReturn(List.of());
        when(context.unfinishedToolCalls()).thenReturn(List.of());
        assertFalse(processor.accepts(context));
    }
    @Test
    void testProcessCallsToolCallService() {
        ToolCall toolCall1 = mock(ToolCall.class);
        ToolCall toolCall2 = mock(ToolCall.class);
        when(context.getToolCalls()).thenReturn(List.of(toolCall1, toolCall2));
        StepContext result = processor.process(context);
        assertSame(context, result);
        verify(toolCallService, times(1)).call(toolCall1);
        verify(toolCallService, times(1)).call(toolCall2);
    }
} 