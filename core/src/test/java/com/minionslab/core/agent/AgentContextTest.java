package com.minionslab.core.agent;

import com.minionslab.core.step.StepManager;
import com.minionslab.core.memory.MemoryManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AgentContext}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Metadata and results management</li>
 * </ul>
 * <p>
 * Setup: Mocks Agent, StepManager, and MemoryManager. Initializes AgentContext.
 */
class AgentContextTest {
    /**
     * Tests that metadata and results can be added and retrieved from AgentContext.
     * Setup: Mocks Agent, StepManager, and MemoryManager. Adds metadata and checks values.
     * Expected: Metadata and results are correctly stored and retrieved.
     */
    @Test
    void testMetadataAndResults() {
        Agent agent = mock(Agent.class);
        when(agent.getRecipe()).thenReturn(mock(AgentRecipe.class));
        when(agent.getAgentId()).thenReturn("id");
        StepManager stepManager = mock(StepManager.class);
        MemoryManager memoryManager = mock(MemoryManager.class);
        AgentContext context = new AgentContext(agent, stepManager, memoryManager);

        context.addMetadata("foo", "bar");
        assertEquals("bar", context.getMetadataValue("foo"));
        assertNotNull(context.getConversationId());
        assertNotNull(context.getResults());
    }
} 