package com.minionslab.core.memory.impl;

import com.minionslab.core.memory.AbstractMemory;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.strategy.persistence.inmemory.InMemoryPersistenceStrategy;

public class ShortTermMemory extends AbstractMemory {
    public ShortTermMemory() {
        super(MemorySubsystem.SHORT_TERM, new InMemoryPersistenceStrategy());
    }
}
