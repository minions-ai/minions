package com.minionslab.core.memory;

import com.minionslab.core.common.message.SimpleMessage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class MemoryRequestTest {
    @Test
    void testDefaultConstructor() {
        MemoryRequest req = new MemoryRequest();
        assertNull(req.getQuery());
        assertNull(req.getMessagesToStore());
    }
    @Test
    void testAllArgsConstructor() {
        MemoryRequest req = new MemoryRequest(null, List.of());
        assertNull(req.getQuery());
        assertEquals(List.of(), req.getMessagesToStore());
    }
    @Test
    void testSetters() {
        MemoryRequest req = new MemoryRequest();
        SimpleMessage foo = SimpleMessage.builder().content("foo").build();
        req.setMessagesToStore(List.of(foo));
        assertEquals(List.of(foo), req.getMessagesToStore());
    }
} 