package com.minionslab.core.memory;

import com.minionslab.core.common.message.SimpleMessage;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MemoryResultTest {
    @Test
    void testConstructionAndGetters() {
        SimpleMessage foo = SimpleMessage.builder().content("foo").build();
        MemoryResult result = new MemoryResult<>("pid", true, List.of(foo), null, Instant.now(), Instant.now());
        assertEquals("pid", result.getProcessorId());
        assertTrue(result.isHandled());
        assertEquals(List.of(foo), result.getResults());
    }
} 