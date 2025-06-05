package com.minionslab.core.step.processor;

import com.minionslab.core.service.ToolCallService;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.tool.ToolCall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ToolCallStepProcessor}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Accepts context with unfinished tool calls</li>
 *   <li>Process calls ToolCallService for each tool call</li>
 * </ul>
 * <p>
 * Setup: Mocks ToolCallService, StepContext, and ToolCall. Initializes ToolCallStepProcessor.
 */
class ToolCallStepProcessorTest {
    /**
     * Sets up the test environment before each test.
     * Mocks ToolCallService and StepContext. Initializes ToolCallStepProcessor.
     * Expected: ToolCallStepProcessor is ready for use in each test.
     */
    ToolCallService toolCallService;
    ToolCallStepProcessor processor;
    StepContext context;
    @BeforeEach
    void setUp() {
        toolCallService = mock(ToolCallService.class);
        processor = new ToolCallStepProcessor(toolCallService);
        context = mock(StepContext.class);
    }
    /**
     * Tests that accepts returns true if there are unfinished tool calls, false otherwise.
     * Setup: StepContext returns tool calls.
     * Expected: accepts returns true if unfinished tool calls exist.
     */
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
    /**
     * Tests that process calls ToolCallService for each tool call.
     * Setup: StepContext returns tool calls, ToolCallService is mocked.
     * Expected: ToolCallService.call is called for each tool call.
     */
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