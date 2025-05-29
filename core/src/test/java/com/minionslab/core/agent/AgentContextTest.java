package com.minionslab.core.agent;

import com.minionslab.core.step.StepManager;
import com.minionslab.core.memory.MemoryManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgentContextTest {
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