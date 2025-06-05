package com.minionslab.core.step.processor;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.StepService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StepCompletionProcessor}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Accepts always returns true</li>
 *   <li>Process delegates to StepService</li>
 * </ul>
 * <p>
 * Setup: Mocks StepService and StepContext. Initializes StepCompletionProcessor.
 */
class StepCompletionProcessorTest {
    StepService stepService;
    StepCompletionProcessor processor;
    StepContext context;

    /**
     * Sets up the test environment before each test.
     * Mocks StepService and StepContext. Initializes StepCompletionProcessor.
     * Expected: StepCompletionProcessor is ready for use in each test.
     */
    @BeforeEach
    void setUp() {
        stepService = mock(StepService.class);
        processor = new StepCompletionProcessor(stepService);
        context = mock(StepContext.class);
    }

    /**
     * Tests that accepts always returns true for any context.
     * Setup: None.
     * Expected: accepts returns true.
     */
    @Test
    void testAcceptsAlwaysTrue() {
        assertTrue(processor.accepts(context));
    }

    /**
     * Tests that process delegates to StepService.executeStep.
     * Setup: Mocks StepService to return the context.
     * Expected: process returns the same context.
     */
    @Test
    void testProcessDelegatesToChainRegistry() {
        when(stepService.executeStep(any(StepContext.class))).thenReturn(context);
        StepContext result = processor.process(context);
        assertSame(context, result);
    }
} 