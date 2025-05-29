package com.minionslab.core.memory.strategy;

import com.minionslab.core.common.chain.ProcessResult;
import com.minionslab.core.memory.MemoryContext;

public interface MemoryPersistanceStrategy extends MemoryStrategy {
    
    boolean accepts(Object context);
    
    void persist(MemoryContext context);
    
    ProcessResult retrieve(MemoryContext context);
    
}
