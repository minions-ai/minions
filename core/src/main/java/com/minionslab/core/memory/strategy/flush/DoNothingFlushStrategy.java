package com.minionslab.core.memory.strategy.flush;

import com.minionslab.core.memory.MemoryContext;
import com.minionslab.core.memory.strategy.MemoryFlushStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DoNothingFlushStrategy implements MemoryFlushStrategy {
    
    
    @Override
    public void flush(MemoryContext context) {
        log.info("Flushing memory was called with input: {}", context);
    }
    
    
}
