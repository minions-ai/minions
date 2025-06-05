package com.minionslab.core.memory;

import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultMemoryTest {
    @Test
    void testConstructorAndDelegation() {
        MemoryPersistenceStrategy strategy = mock(MemoryPersistenceStrategy.class);
        DefaultMemory memory = new DefaultMemory(MemorySubsystem.ENTITY, strategy);
        assertEquals(MemorySubsystem.ENTITY, memory.getMemorySubsystem());
    }
} 