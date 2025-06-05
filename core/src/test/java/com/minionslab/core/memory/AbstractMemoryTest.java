package com.minionslab.core.memory;

import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;
import com.minionslab.core.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractMemoryTest {
    private MemoryPersistenceStrategy persistenceStrategy;
    private DefaultMemory memory;
    @BeforeEach
    void setUp() {
        persistenceStrategy = mock(MemoryPersistenceStrategy.class);
        memory = new DefaultMemory(MemorySubsystem.ENTITY, persistenceStrategy);
    }
    @Test
    void testStoreAndRetrieve() {
        Message msg = mock(Message.class);
        when(persistenceStrategy.save(msg)).thenReturn(msg);
        memory.store(msg);
        when(persistenceStrategy.findById("id", Message.class)).thenReturn(Optional.of(msg));
        assertEquals(msg, memory.retrieve("id"));
    }
    @Test
    void testStoreAll() {
        Message msg = mock(Message.class);
        List<Message> msgs = List.of(msg);
        when(persistenceStrategy.saveAll(msgs)).thenReturn(msgs);
        memory.storeAll(msgs);
        verify(persistenceStrategy).saveAll(msgs);
    }
    @Test
    void testDeleteById() {
        when(persistenceStrategy.deleteById("id", Message.class)).thenReturn(true);
        assertTrue(memory.deleteById("id"));
    }
    @Test
    void testFlush() {
        // Should just log, not throw
        assertDoesNotThrow(() -> memory.flush());
    }
    @Test
    void testSnapshotRestoreNotSupported() {
        assertDoesNotThrow(() -> memory.snapshot());
        assertDoesNotThrow(() -> memory.restoreLatestSnapshot());
    }
    @Test
    void testAcceptsAlwaysTrue() {
        assertTrue(memory.accepts(mock(MemoryContext.class)));
    }
} 