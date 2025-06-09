package com.minionslab.core.memory.strategy.query.agent;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.query.MemoryQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgentVectorQueryStrategyTest {

    @Mock
    private AgentContext agentContext;

    private AgentVectorQueryStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new AgentVectorQueryStrategy();
    }

    @Test
    void testGetMemoryQueryReturnsValidQuery() {
        MemoryQuery query = strategy.getMemoryQuery(agentContext);
        assertNotNull(query);
    }

    @Test
    void testAcceptsReturnsTrueForAgentContext() {
        assertTrue(strategy.accepts(agentContext));
    }

    @Test
    void testAcceptsReturnsFalseForNonAgentContext() {
        ProcessContext otherContext = mock(ProcessContext.class);
        assertFalse(strategy.accepts(otherContext));
    }
} 