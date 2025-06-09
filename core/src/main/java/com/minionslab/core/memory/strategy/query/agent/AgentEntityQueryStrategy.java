package com.minionslab.core.memory.strategy.query.agent;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.expression.AlwaysTrueExpression;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentEntityQueryStrategy implements MemoryQueryStrategy<AgentContext> {
    @Override
    public MemoryQuery getMemoryQuery(AgentContext context) {
        return MemoryQuery.builder()
                          .limit(AgentContext.getConfig().getQueryConfig().getLimit())
                          .expression(new AlwaysTrueExpression())
                          .build();
        
    }
    
    @Override
    public List<MemorySubsystem> getSupportedSubsystem() {
        return List.of(MemorySubsystem.ENTITY);
    }
    
    @Override
    public boolean accepts(ProcessContext processContext) {
        return processContext != null && processContext instanceof AgentContext;
    }
}
