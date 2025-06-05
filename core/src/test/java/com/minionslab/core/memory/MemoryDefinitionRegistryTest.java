package com.minionslab.core.memory;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DummyMemoryDefinition implements MemoryDefinition {
    @Override public List<String> getQueryStrategies() { return List.of(); }
    @Override public String getPersistStrategy() { return "persist"; }
    @Override public String getFlushStrategy() { return "flush"; }
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