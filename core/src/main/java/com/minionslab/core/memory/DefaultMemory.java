package com.minionslab.core.memory;

import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;

/**
 * DefaultMemory is a simple concrete implementation of AbstractMemory.
 * It is used as a generic memory type when no specialized subclass is needed.
 */
public class DefaultMemory extends AbstractMemory {
    public DefaultMemory(MemorySubsystem memorySubsystem, MemoryPersistenceStrategy persistenceStrategy) {
        super(memorySubsystem, persistenceStrategy);
    }
    // Optionally override flush or other methods if needed
} 