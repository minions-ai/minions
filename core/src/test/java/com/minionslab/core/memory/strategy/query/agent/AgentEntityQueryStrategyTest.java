package com.minionslab.core.memory.strategy.query.agent;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.agent.AgentConfig;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.QueryConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentEntityQueryStrategyTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    private AgentContext agentContext;
    
    private MockedStatic<AgentContext> agentContextMockedStatic;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private QueryConfig queryConfig;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private AgentConfig agentConfig;

    private AgentEntityQueryStrategy strategy;

    @BeforeEach
    void setUp() {
        agentContextMockedStatic = mockStatic(AgentContext.class);
        strategy = new AgentEntityQueryStrategy();
        when(AgentContext.getConfig()).thenReturn(agentConfig);
        when(agentConfig.getQueryConfig()).thenReturn(queryConfig);
        when(queryConfig.getLimit()).thenReturn(10);
    }

    @AfterEach
    void tearDown() {
        agentContextMockedStatic.close();
    }
    @Test
    void testGetMemoryQueryReturnsValidQuery() {
        MemoryQuery query = strategy.getMemoryQuery(agentContext);
        assertNotNull(query);
        assertEquals(10, query.getLimit());
        assertNotNull(query.getExpression());
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