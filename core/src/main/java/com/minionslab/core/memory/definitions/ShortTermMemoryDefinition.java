package com.minionslab.core.memory.definitions;

import com.minionslab.core.memory.MemoryDefinition;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.strategy.MemoryFlushStrategy;
import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;
import com.minionslab.core.memory.strategy.flush.DoNothingFlushStrategy;
import com.minionslab.core.memory.strategy.persistence.inmemory.InMemoryPersistenceStrategy;
import org.springframework.stereotype.Component;

@Component
public class ShortTermMemoryDefinition implements MemoryDefinition {
    
    
    @Override
    public MemoryPersistenceStrategy getPersistStrategy() {
        return new InMemoryPersistenceStrategy();
    }
    
    @Override
    public MemoryFlushStrategy getFlushStrategy() {
        return new DoNothingFlushStrategy() {
        };
    }
    
    @Override
    public String getMemoryRole() {
        return MemorySubsystem.SHORT_TERM.name();
    }
    
    @Override
    public MemorySubsystem getMemorySubsystem() {
        return MemorySubsystem.SHORT_TERM;
    }
    
    @Override
    public String getMemoryName() {
        return "";
    }
}
