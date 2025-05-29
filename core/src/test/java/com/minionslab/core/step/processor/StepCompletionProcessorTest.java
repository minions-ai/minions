package com.minionslab.core.step.processor;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.step.StepContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StepCompletionProcessorTest {
    ChainRegistry chainRegistry;
    StepCompletionProcessor processor;
    StepContext context;
    @BeforeEach
    void setUp() {
        chainRegistry = mock(ChainRegistry.class);
        processor = new StepCompletionProcessor(chainRegistry);
        context = mock(StepContext.class);
    }
    @Test
    void testAcceptsAlwaysTrue() {
        assertTrue(processor.accepts(context));
    }
    @Test
    void testProcessDelegatesToChainRegistry() {
        when(chainRegistry.process(context)).thenReturn(context);
        StepContext result = processor.process(context);
        assertSame(context, result);
        verify(chainRegistry).process(context);
    }
} 