package com.minionslab.core.memory.strategy.query.step;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.expression.AlwaysTrueExpression;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;

import java.util.List;

public class StepEntityQueryStrategy implements MemoryQueryStrategy<StepContext> {
    @Override
    public MemoryQuery getMemoryQuery(StepContext context) {
        return MemoryQuery.builder()
                          .limit(StepContext.getConfig().getQueryConfig().getLimit())
                          .expression(new AlwaysTrueExpression())
                          .build();
        
    }
    
    @Override
    public List<MemorySubsystem> getSupportedSubsystem() {
        return List.of(MemorySubsystem.ENTITY);
    }
    
    @Override
    public boolean accepts(ProcessContext processContext) {
        return processContext != null && processContext instanceof StepContext;
    }
}
