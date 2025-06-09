package com.minionslab.core.common.util;

import com.minionslab.core.common.message.MessageRole;
import com.minionslab.core.common.message.SimpleMessage;
import com.minionslab.core.tool.ToolCall;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageConverterTest {
    @Test
    void testToMCPMessageSystemUserAssistant() {
        SystemMessage sys = new SystemMessage("sys");
        UserMessage user = new UserMessage("user");
        AssistantMessage assistant = new AssistantMessage("assistant");
        assertEquals(MessageRole.SYSTEM, MessageConverter.toMCPMessage(sys).getRole());
        assertEquals(MessageRole.USER, MessageConverter.toMCPMessage(user).getRole());
        assertEquals(MessageRole.ASSISTANT, MessageConverter.toMCPMessage(assistant).getRole());
    }

/*    @Test
    void testToMCPMessageThrowsOnUnknownType() {
        var unknown = mock(org.springframework.ai.chat.messages.Message.class);
        when(unknown.getContent()).thenReturn("unknown");
        assertThrows(IllegalArgumentException.class, () -> MessageConverter.toMCPMessage(unknown));
    }*/

    @Test
    void testToSpringMessageCoversAllRoles() {
        for (MessageRole role : MessageRole.values()) {
            SimpleMessage msg = SimpleMessage.builder().role(role).content("c").build();
            if (role == MessageRole.USER || role == MessageRole.ASSISTANT || role == MessageRole.SYSTEM || role == MessageRole.ERROR || role == MessageRole.TOOL || role == MessageRole.GOAL) {
                assertNotNull(MessageConverter.toSpringMessage(msg));
            } else {
                assertThrows(IllegalArgumentException.class, () -> MessageConverter.toSpringMessage(msg));
            }
        }
    }

    @Test
    void testCreateErrorMessage() {
        Exception e = new RuntimeException("fail");
        var msg = MessageConverter.createErrorMessage(e);
        assertEquals(MessageRole.ERROR, msg.getRole());
        assertTrue(msg.getContent().contains("fail"));
    }

    @Test
    void testFromSpringToolCall() {
        AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall("id", "tool", "tool",null);
        ToolCall result = MessageConverter.fromSpringToolCall(toolCall);
        assertNotNull(result);
        assertEquals("tool", result.getName());
    }

    @Test
    void testToMCPMessagesAndToSpringMessages() {
        List<org.springframework.ai.chat.messages.Message> springMsgs = List.of(new SystemMessage("sys"), new UserMessage("user"));
        var mcpMsgs = MessageConverter.toMCPMessages(springMsgs);
        assertEquals(2, mcpMsgs.size());
        var springMsgs2 = MessageConverter.toSpringMessages(mcpMsgs);
        assertEquals(2, springMsgs2.size());
    }
} 