package com.minionslab.core.memory.strategy.query.agent;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;

import java.util.List;

public class AgentVectorQueryStrategy implements MemoryQueryStrategy<AgentContext>{
    @Override
    public MemoryQuery getMemoryQuery(AgentContext context) {
        return MemoryQuery.builder().build();
    }
    
    @Override
    public List<MemorySubsystem> getSupportedSubsystem() {
        return List.of();
    }
    
    @Override
    public boolean accepts(ProcessContext processContext) {
        return processContext != null && processContext instanceof AgentContext;
    }
}
