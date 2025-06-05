package com.minionslab.core.memory.strategy.query;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.common.chain.AbstractProcessor;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.MemoryContext;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;

public abstract class AbstractSmartMemoryQueryStrategy<T extends ProcessContext> extends AbstractProcessor<MemoryContext, MemoryQuery> implements MemoryQueryStrategy {
    @Override
    protected MemoryQuery doProcess(MemoryContext input) throws Exception {
        if (!(input.getSourceContext() instanceof AgentContext)) {
            throw new IllegalArgumentException("Invalid source context for AgentSmartMemoryQueryStrategy");
        }
        T agentContext = (T) input.getSourceContext();

        MemorySubsystem subsystem = input.getMemorySubsystem();
        MemoryQuery query = switch (subsystem) {
            case SHORT_TERM -> getShortTermQuery(agentContext);
            case VECTOR -> getVectorQuery(agentContext);
            case ENTITY -> getEntityQuery(agentContext);
            case EPISODIC -> getEpisodicQuery(agentContext);
            case MEMORY_MANAGER -> null;
            case LONG_TERM -> getLongTermQuery(agentContext);
        };
        return query;
    }
    
    public abstract MemoryQuery getLongTermQuery(T agentContext);
    
    public abstract MemoryQuery getEpisodicQuery(T agentContext);
    
    public abstract MemoryQuery getEntityQuery(T agentContext);
    
    public abstract MemoryQuery getVectorQuery(T agentContext);
    
    public abstract MemoryQuery getShortTermQuery(T agentContext);
    
    public String getName() {
        return this.getClass().getSimpleName();
    }
    
}
