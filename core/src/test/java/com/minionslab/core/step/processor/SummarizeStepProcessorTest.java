package com.minionslab.core.step.processor;

import com.minionslab.core.memory.MemoryManager;
import com.minionslab.core.memory.query.MemoryQueryUtils;
import com.minionslab.core.message.Message;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.step.StepContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SummarizeStepProcessor}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Accepts always returns true</li>
 * </ul>
 * <p>
 * Setup: Mocks StepContext. Initializes SummarizeStepProcessor.
 */
@ExtendWith(MockitoExtension.class)
class SummarizeStepProcessorTest {
    @Mock StepContext context;
    @Mock MemoryManager memoryManager;
    /**
     * Tests that accepts always returns true for any context.
     * Setup: Mocks StepContext.
     * Expected: accepts returns true.
     */
    @Test
    void testAcceptsAlwaysTrue() {
        SummarizeStepProcessor proc = new SummarizeStepProcessor();
        assertTrue(proc.accepts(context));
    }
    @Test
    void testProcessAddsModelCall() {
        SummarizeStepProcessor proc = new SummarizeStepProcessor();
        when(context.getMemoryManager()).thenReturn(memoryManager);
        when(context.getMetadata()).thenReturn(Map.of());
        // Mock static MemoryQueryUtils
        try (var mocked = mockStatic(MemoryQueryUtils.class)) {
            Message m1 = mock(Message.class), m2 = mock(Message.class);
            when(m1.getTimestamp()).thenReturn(java.time.Instant.now());
            when(m2.getTimestamp()).thenReturn(java.time.Instant.now().plusSeconds(1));
            mocked.when(() -> MemoryQueryUtils.getLastNUserMessages(memoryManager, 10)).thenReturn(List.of(m1));
            mocked.when(() -> MemoryQueryUtils.getLastNAssistantMessages(memoryManager, 10)).thenReturn(List.of(m2));
            doAnswer(inv -> { 
                ModelCall mc = inv.getArgument(0);
                assertNotNull(mc);
                return null;
            }).when(context).addModelCall(any());
            StepContext result = proc.process(context);
            assertSame(context, result);
            verify(context).addModelCall(any(ModelCall.class));
        }
    }
} 