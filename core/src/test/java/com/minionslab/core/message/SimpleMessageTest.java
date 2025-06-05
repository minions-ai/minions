package com.minionslab.core.message;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimpleMessageTest {
    @Test
    void testConstructionAndGetters() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("foo", "bar");
        
        SimpleMessage msg = SimpleMessage.builder().id("id").timestamp(Instant.now()).metadata(meta).content("content").role(MessageRole.USER).scope(MessageScope.AGENT).tokenCount(5).build();
        assertEquals("id", msg.getId());
        assertEquals("content", msg.getContent());
        assertEquals(MessageRole.USER, msg.getRole());
        assertEquals(MessageScope.AGENT, msg.getScope());
        assertEquals(5, msg.getTokenCount());
        assertEquals("bar", msg.getMetadata().get("foo"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        SimpleMessage msg1 = SimpleMessage.builder().id("id").content("c").role(MessageRole.USER).scope(MessageScope.AGENT).build();
        SimpleMessage msg2 = SimpleMessage.builder().id("id").content("c").role(MessageRole.USER).scope(MessageScope.AGENT).build();
        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }
    
    @Test
    void testToPromptString() {
        SimpleMessage msg = SimpleMessage.builder().content("hello").role(MessageRole.SYSTEM).build();
        assertTrue(msg.toPromptString().contains("hello"));
    }
    
    @Test
    void testToStringReturnsId() {
        SimpleMessage msg = SimpleMessage.builder().id("id123").build();
        assertEquals("id123", msg.toString());
    }
    
    @Test
    void testBuilderWithNulls() {
        SimpleMessage msg = SimpleMessage.builder().build();
        assertNull(msg.getContent());
        assertNull(msg.getRole());
    }
} 