package com.minionslab.core.memory.strategy;

import com.minionslab.core.memory.MemoryContext;
import com.minionslab.core.memory.MemoryOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * NoOpStrategy: a MemoryStrategy that does nothing for the given operation.
 */
@Slf4j
class NoOpStrategy implements MemoryStrategy {
    private final MemoryOperation operation;
    
    public NoOpStrategy(MemoryOperation operation) {
        this.operation = operation;
    }
    
    @Override
    public String getName() {
        return "NoOpStrategy-" + operation.name();
    }
    
    @Override
    public List<MemoryOperation> getOperationsSupported() {
        return singletonList(operation);
    }
    
    
    @Override
    public boolean accepts(MemoryContext input) {
        return true;
    }
    
    @Override
    public MemoryContext process(MemoryContext input) {
        log.info("NoOpStrategy process was called for input {}", input);
        return input;
    }
}
