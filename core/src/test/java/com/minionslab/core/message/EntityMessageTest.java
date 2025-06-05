package com.minionslab.core.message;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class EntityMessageTest {
    @Test
    void testConstructionAndGetters() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("foo", "bar");
        EntityMessage msg = EntityMessage.builder().id("id").timestamp(Instant.now()).metadata(meta).content("entity content").role(MessageRole.USER).scope(MessageScope.AGENT).tokenCount(3).entity(Map.of("k","v")).build();
        assertEquals("id", msg.getId());
        assertEquals("entity content", msg.getContent());
        assertEquals(MessageRole.USER, msg.getRole());
        assertEquals(MessageScope.AGENT, msg.getScope());
        assertEquals(3, msg.getTokenCount());
        assertEquals("bar", msg.getMetadata().get("foo"));
        assertEquals("v", ((Map<?,?>)msg.getEntity()).get("k"));
    }

    @Test
    void testEqualsAndHashCode() {
        EntityMessage msg1 = EntityMessage.builder().id("id").content("c").role(MessageRole.USER).scope(MessageScope.AGENT).entity("e").build();
        EntityMessage msg2 = EntityMessage.builder().id("id").content("c").role(MessageRole.USER).scope(MessageScope.AGENT).entity("e").build();
        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    void testToPromptString() {
        EntityMessage msg = EntityMessage.builder().content("hello").role(MessageRole.SYSTEM).entity("ent").build();
        assertTrue(msg.toPromptString().contains("hello"));
        assertTrue(msg.toPromptString().contains("ent"));
    }

    @Test
    void testBuilderWithNullEntity() {
        EntityMessage msg = EntityMessage.builder().build();
        assertNull(msg.getEntity());
    }
} 