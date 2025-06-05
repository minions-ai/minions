package com.minionslab.core.agent;

import com.minionslab.core.message.Message;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultAgent}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Get available tools from agent recipe</li>
 * </ul>
 * <p>
 * Setup: Mocks AgentRecipe and Message. Initializes DefaultAgent.
 */
class DefaultAgentTest {
    /**
     * Tests that getAvailableTools returns the required tools from the agent recipe.
     * Setup: Mocks AgentRecipe to return a list of tools.
     * Expected: getAvailableTools returns the correct list.
     */
    @Test
    void testGetAvailableTools() {
        AgentRecipe recipe = mock(AgentRecipe.class);
        Message userMessage = mock(Message.class);
        when(recipe.getRequiredTools()).thenReturn(List.of("tool1", "tool2"));
        DefaultAgent agent = new DefaultAgent(recipe, userMessage);
        assertEquals(List.of("tool1", "tool2"), agent.getAvailableTools());
    }
} 