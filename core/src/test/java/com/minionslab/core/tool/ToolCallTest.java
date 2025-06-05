package com.minionslab.core.tool;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ToolCall} and its inner classes.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Construction and accessors for ToolCall, ToolCallRequest, ToolCallResponse</li>
 *   <li>Default status and result handling</li>
 * </ul>
 * <p>
 * Setup: Instantiates ToolCall and related classes with test data.
 */
class ToolCallTest {
    /**
     * Tests construction and accessors for ToolCall, ToolCallRequest, and ToolCallResponse.
     * Setup: Creates ToolCallRequest, ToolCallResponse, and ToolCall with test data.
     * Expected: Accessors return correct values.
     */
    @Test
    void testToolCallConstructionAndAccessors() {
        ToolCall.ToolCallRequest request = new ToolCall.ToolCallRequest( "input", Map.of("param", "value"));
        ToolCall.ToolCallResponse response = new ToolCall.ToolCallResponse("result", null);
        ToolCall toolCall = ToolCall.builder()
            .name("tool")
            .request(request)
            .response(response)
            .build();

        assertEquals("tool", toolCall.getName());
        assertEquals(request, toolCall.getRequest());
        assertEquals(response, toolCall.getResponse());
        assertEquals(ToolCallStatus.PENDING, toolCall.getStatus());
    }

    /**
     * Tests that ToolCall has the correct default status.
     * Setup: Instantiates ToolCall.
     * Expected: Status is PENDING by default.
     */
    @Test
    void testToolCallDefaultStatus() {
        ToolCall toolCall = new ToolCall();
        assertEquals(ToolCallStatus.PENDING, toolCall.getStatus());
    }

    /**
     * Tests result handling methods in ToolCall.
     * Setup: Instantiates ToolCall and calls result methods.
     * Expected: Results list is managed correctly and no exceptions are thrown.
     */
    @Test
    void testToolCallProcessContextMethods() {
        ToolCall toolCall = new ToolCall();
        assertTrue(toolCall.getResults().isEmpty());
        assertDoesNotThrow(() -> toolCall.addResult(null));
    }
} 