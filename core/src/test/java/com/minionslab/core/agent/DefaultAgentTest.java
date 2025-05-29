package com.minionslab.core.agent;

import com.minionslab.core.message.Message;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultAgentTest {
    @Test
    void testGetAvailableTools() {
        AgentRecipe recipe = mock(AgentRecipe.class);
        Message userMessage = mock(Message.class);
        when(recipe.getRequiredTools()).thenReturn(List.of("tool1", "tool2"));
        DefaultAgent agent = new DefaultAgent(recipe, userMessage);
        assertEquals(List.of("tool1", "tool2"), agent.getAvailableTools());
    }
} 