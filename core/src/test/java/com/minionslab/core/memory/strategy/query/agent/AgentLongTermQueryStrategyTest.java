package com.minionslab.core.memory.strategy.query.agent;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.agent.AgentConfig;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.QueryConfig;
import com.minionslab.core.agent.AgentRecipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgentLongTermQueryStrategyTest {

    private MockedStatic<AgentContext> agentContextMockedStatic;
    @Mock
    private AgentContext agentContext;
    @Mock
    private QueryConfig queryConfig;
    @Mock
    private AgentConfig agentConfig;
    @Mock
    private AgentRecipe recipe;

    private AgentLongTermQueryStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        agentContextMockedStatic = mockStatic(AgentContext.class);
        strategy = new AgentLongTermQueryStrategy();
        when(agentContext.getRecipe()).thenReturn(recipe);
        when(recipe.getId()).thenReturn("testRecipeId");
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