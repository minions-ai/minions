package com.minionslab.core.tool;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ToolCallTest {
    @Test
    void testToolCallConstructionAndAccessors() {
        ToolCall.ToolCallRequest request = new ToolCall.ToolCallRequest("tool", "input", Map.of("param", "value"));
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

    @Test
    void testToolCallDefaultStatus() {
        ToolCall toolCall = new ToolCall();
        assertEquals(ToolCallStatus.PENDING, toolCall.getStatus());
    }

    @Test
    void testToolCallProcessContextMethods() {
        ToolCall toolCall = new ToolCall();
        assertTrue(toolCall.getResults().isEmpty());
        assertDoesNotThrow(() -> toolCall.addResult(null));
    }
} 