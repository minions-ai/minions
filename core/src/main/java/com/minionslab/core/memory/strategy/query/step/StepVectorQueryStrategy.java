package com.minionslab.core.memory.strategy.query.step;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;
import com.minionslab.core.step.StepContext;

import java.util.List;

public class StepVectorQueryStrategy implements MemoryQueryStrategy<StepContext> {
    @Override
    public MemoryQuery getMemoryQuery(StepContext context) {
        return MemoryQuery.builder().build();
    }
    
    @Override
    public List<MemorySubsystem> getSupportedSubsystem() {
        return List.of();
    }
    
    @Override
    public boolean accepts(ProcessContext processContext) {
        return processContext != null && processContext instanceof StepContext;
    }
}
