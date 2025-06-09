package com.minionslab.core.memory;

import com.minionslab.core.memory.strategy.MemoryFlushStrategy;
import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DummyMemoryDefinition implements MemoryDefinition {

    @Override public MemoryPersistenceStrategy getPersistStrategy() { return mock(MemoryPersistenceStrategy.class); }
    @Override public MemoryFlushStrategy getFlushStrategy() { return mock(MemoryFlushStrategy.class); }
    @Override public String getMemoryRole() { return "role"; }
    @Override public String getMemoryName() { return "dummy"; }
    @Override public MemorySubsystem getMemorySubsystem() { return MemorySubsystem.ENTITY; }
}

class MemoryDefinitionRegistryTest {
    @Test
    void testRegistrationAndLookup() {
        DummyMemoryDefinition def = new DummyMemoryDefinition();
        MemoryDefinitionRegistry registry = new MemoryDefinitionRegistry(List.of(def));
        assertEquals(def, registry.getMemoryDefinition(MemorySubsystem.ENTITY));
    }
    @Test
    void testLookupMissingReturnsNull() {
        MemoryDefinitionRegistry registry = new MemoryDefinitionRegistry(List.of());
        assertNull(registry.getMemoryDefinition(MemorySubsystem.ENTITY));
    }
} 