package com.minionslab.core.memory.strategy.query.step;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.step.StepContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StepVectorQueryStrategyTest {

    @Mock
    private StepContext stepContext;

    private StepVectorQueryStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new StepVectorQueryStrategy();
    }

    @Test
    void testGetMemoryQueryReturnsValidQuery() {
        MemoryQuery query = strategy.getMemoryQuery(stepContext);
        assertNotNull(query);
    }

    @Test
    void testAcceptsReturnsTrueForStepContext() {
        assertTrue(strategy.accepts(stepContext));
    }

    @Test
    void testAcceptsReturnsFalseForNonStepContext() {
        ProcessContext otherContext = mock(ProcessContext.class);
        assertFalse(strategy.accepts(otherContext));
    }
} 