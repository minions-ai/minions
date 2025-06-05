package com.minionslab.core.memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemoryContextTest {
    @Test
    void testSetAndGetMemoryRequest() {
        MemoryContext ctx = new MemoryContext();
        MemoryRequest req = new MemoryRequest();
        ctx.setMemoryRequest(req);
        assertEquals(req, ctx.getMemoryRequest());
    }
    @Test
    void testSetAndGetOperation() {
        MemoryContext ctx = new MemoryContext();
        ctx.setOperation(MemoryOperation.STORE);
        assertEquals(MemoryOperation.STORE, ctx.getOperation());
    }
    @Test
    void testAddAndGetResults() {
        MemoryContext ctx = new MemoryContext();
        MemoryResult result = new MemoryResult("id", true, null, null, null, null);
        ctx.addResult(result);
        assertTrue(ctx.getResults().contains(result));
    }
} 