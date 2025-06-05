package com.minionslab.core.memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageNotFoundExceptionTest {
    @Test
    void testExceptionMessage() {
        MessageNotFoundException ex = new MessageNotFoundException("not found");
        assertEquals("not found", ex.getMessage());
    }
} 