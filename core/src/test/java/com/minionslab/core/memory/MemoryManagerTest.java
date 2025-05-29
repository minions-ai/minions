package com.minionslab.core.memory;

import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemoryManagerTest {
    private Processor<MemoryContext> memoryProcessor;
    private MemoryManager manager;

    @BeforeEach
    void setUp() {
        memoryProcessor = mock(Processor.class);
        manager = new MemoryManager(List.of(memoryProcessor));
    }

    @Test
    void testStoreDelegatesToChain() {
        Message message = mock(Message.class);
        manager.store(message);
        verify(memoryProcessor, atLeastOnce()).process(any(MemoryContext.class));
    }

    @Test
    void testRetrieveReturnsNullIfNoMessages() {
        when(memoryProcessor.process(any())).thenReturn(new MemoryContext());
        assertNull(manager.retrieve("id"));
    }

    @Test
    void testFlushDelegatesToChain() {
        manager.flush();
        verify(memoryProcessor, atLeastOnce()).process(any(MemoryContext.class));
    }

    @Test
    void testQueryDelegatesToChain() {
        when(memoryProcessor.process(any())).thenReturn(new MemoryContext());
        assertNotNull(manager.query(mock(MemoryQuery.class)));
    }

    @Test
    void testProcessDelegatesToChain() {
        MemoryContext ctx = new MemoryContext();
        when(memoryProcessor.process(ctx)).thenReturn(ctx);
        assertEquals(ctx, manager.process(ctx));
    }
} 