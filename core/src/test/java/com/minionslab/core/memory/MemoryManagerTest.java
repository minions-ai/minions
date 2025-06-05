package com.minionslab.core.memory;

import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MemoryManager}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Delegates store, retrieve, flush, query, and process to memory processor chain</li>
 * </ul>
 * <p>
 * Setup: Mocks Processor and initializes MemoryManager with it.
 */
class MemoryManagerTest {
    private Processor<MemoryContext> memoryProcessor;
    private MemoryManager manager;

    @BeforeEach
    void setUp() {
        memoryProcessor = mock(Processor.class);
        manager = new MemoryManager(List.of(memoryProcessor));
    }

    /**
     * Tests that store delegates to the memory processor chain.
     * Setup: Mocks Processor and Message.
     * Expected: Processor's process method is called.
     */
    @Test
    void testStoreDelegatesToChain() {
        Message message = mock(Message.class);
        manager.store(message);
        verify(memoryProcessor, atLeastOnce()).process(any(MemoryContext.class));
    }

    /**
     * Tests that retrieve returns null if no messages are found.
     * Setup: Processor returns a new MemoryContext.
     * Expected: retrieve returns null.
     */
    @Test
    void testRetrieveReturnsNullIfNoMessages() {
        when(memoryProcessor.process(any())).thenReturn(new MemoryContext());
        assertNull(manager.retrieve("id"));
    }

    /**
     * Tests that flush delegates to the memory processor chain.
     * Setup: Mocks Processor.
     * Expected: Processor's process method is called.
     */
    @Test
    void testFlushDelegatesToChain() {
        manager.flush();
        verify(memoryProcessor, atLeastOnce()).process(any(MemoryContext.class));
    }

    /**
     * Tests that query delegates to the memory processor chain.
     * Setup: Processor returns a new MemoryContext.
     * Expected: query returns a non-null result.
     */
    @Test
    void testQueryDelegatesToChain() {
        when(memoryProcessor.process(any())).thenReturn(new MemoryContext());
        assertNotNull(manager.query(mock(MemoryQuery.class)));
    }

    /**
     * Tests that process delegates to the memory processor chain.
     * Setup: Processor returns the same MemoryContext.
     * Expected: process returns the same MemoryContext.
     */
    @Test
    void testProcessDelegatesToChain() {
        MemoryContext ctx = new MemoryContext();
        when(memoryProcessor.process(ctx)).thenReturn(ctx);
        assertEquals(ctx, manager.process(ctx));
    }
} 