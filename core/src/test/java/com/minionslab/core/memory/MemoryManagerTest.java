package com.minionslab.core.memory;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
@ExtendWith(MockitoExtension.class)
class MemoryManagerTest {
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Memory<MemoryContext, Message> memoryProcessor;
    private MemoryManager manager;
    
    
    @Mock(strictness =Mock.Strictness.LENIENT)
    private Memory<MemoryContext, Message> memory1;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Memory<MemoryContext, Message> memory2;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private MemoryQueryStrategy strategy1;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private MemoryQueryStrategy strategy2;
    
    @Mock(strictness = Mock.Strictness.LENIENT)
    private MemoryPersistenceStrategy persistenceStrategy;
    
    @Mock(strictness = Mock.Strictness.LENIENT)
    private ProcessContext processContext;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private MemoryQuery memoryQuery;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Message message1;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Message message2;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        List<Memory<MemoryContext, Message>> memories = new ArrayList<>();
        memories.add(memory1);
        memories.add(memory2);
        List<MemoryQueryStrategy> strategies = new ArrayList<>();
        strategies.add(strategy1);
        strategies.add(strategy2);
        manager = new MemoryManager(memories, strategies);
    }
    
    /**
     * Tests that store delegates to the memory processor chain.
     * Setup: Mocks Processor and Message.
     * Expected: Processor's process method is called.
     */
    @Test
    void testStoreDelegatesToChain() {
        Message message = mock(Message.class);
        when(memory1.getMemorySubsystem()).thenReturn(MemorySubsystem.SHORT_TERM);
        when(memory2.getMemorySubsystem()).thenReturn(MemorySubsystem.LONG_TERM);
        manager.store(memory1.getMemorySubsystem(),message);
        verify(memory1, atLeastOnce()).store(any(Message.class));
        verify(memory2,atMost(0)).store(any(Message.class));
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
        verify(memory1, atLeastOnce()).flush();
        verify(memory2, atLeastOnce()).flush();
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
    
    @Test
    void testQueryConsolidatesResults() {
        when(strategy1.getOperationsSupported()).thenReturn(Collections.singletonList(MemoryOperation.QUERY));
        when(strategy1.getSupportedSubsystem()).thenReturn(Collections.singletonList(MemorySubsystem.MEMORY_MANAGER));
        when(strategy1.getMemoryQuery(processContext)).thenReturn(memoryQuery);
        when(memory1.query(memoryQuery)).thenReturn(Collections.singletonList(message1));
        when(memory2.query(memoryQuery)).thenReturn(Collections.singletonList(message2));
        
        List<Message> results = manager.query(processContext);
        
        assertEquals(2, results.size());
        assertTrue(results.contains(message1));
        assertTrue(results.contains(message2));
    }
    
    @Test
    void testStoreAllDelegatesToCorrectSubsystem() {
        List<Message> messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);
        when(memory1.getMemorySubsystem()).thenReturn(MemorySubsystem.MEMORY_MANAGER);
        when(memory2.getMemorySubsystem()).thenReturn(MemorySubsystem.ENTITY);
        
        manager.storeAll(messages, MemorySubsystem.MEMORY_MANAGER);
        
        verify(memory1).storeAll(messages);
        verify(memory2, never()).storeAll(messages);
    }
    
    @Test
    void testFlushDelegatesToAllMemories() {
        manager.flush();
        verify(memory1).flush();
        verify(memory2).flush();
    }
    
    @Test
    void testRetrieveReturnsFirstMessage() {
        String id = "testId";
        when(persistenceStrategy.findById(id, Message.class)).thenReturn(Optional.of(message1));
        when(memory1.retrieve(id)).thenReturn(message1);
        when(memory2.retrieve(id)).thenReturn(message2);
        
        Message result = manager.retrieve(id);
        
        assertEquals(message1, result);
    }
    
    @Test
    void testAcceptsReturnsTrueForNonNullInput() {
        assertTrue(manager.accepts(processContext));
    }
} 