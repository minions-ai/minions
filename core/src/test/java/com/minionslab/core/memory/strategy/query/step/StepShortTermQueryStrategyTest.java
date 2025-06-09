package com.minionslab.core.memory.strategy.query.step;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.QueryConfig;
import com.minionslab.core.memory.strategy.MemoryStrategy;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.agent.AgentConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StepShortTermQueryStrategyTest {

    private MockedStatic<AgentContext> agentContextMockedStatic;
    @Mock
    private StepContext stepContext;
    @Mock
    private QueryConfig queryConfig;

    private StepShortTermQueryStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        agentContextMockedStatic = mockStatic(AgentContext.class);
        strategy = new StepShortTermQueryStrategy();
        when(stepContext.getConversationId()).thenReturn("testConversationId");
        AgentConfig agentConfig = mock(AgentConfig.class);
        when(agentConfig.getQueryConfig()).thenReturn(queryConfig);
        when(StepContext.getConfig()).thenReturn(agentConfig);
        when(queryConfig.getLimit()).thenReturn(10);
    }

    @AfterEach
    void tearDown() {
        agentContextMockedStatic.close();
    }
    @Test
    void testGetMemoryQueryReturnsValidQuery() {
        MemoryQuery query = strategy.getMemoryQuery(stepContext);
        assertNotNull(query);
        assertEquals(10, query.getLimit());
        assertNotNull(query.getExpression());
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