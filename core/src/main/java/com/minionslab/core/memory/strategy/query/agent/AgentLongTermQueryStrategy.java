package com.minionslab.core.memory.strategy.query.agent;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.QueryConfig;
import com.minionslab.core.memory.query.expression.Expr;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class AgentLongTermQueryStrategy implements MemoryQueryStrategy<AgentContext> {
    
    
    @Override
    public MemoryQuery getMemoryQuery(AgentContext context) {
        QueryConfig queryConfig = AgentContext.getConfig().getQueryConfig();
        return MemoryQuery.builder().
                          limit(queryConfig.getLimit())
                          .expression(Expr.eq("recipeId", context.getRecipe().getId()))
                          .build();
    }
    
    @Override
    public List<MemorySubsystem> getSupportedSubsystem() {
        return List.of(MemorySubsystem.LONG_TERM);
    }
    
    @Override
    public boolean accepts(ProcessContext processContext) {
        return processContext != null && processContext instanceof AgentContext;
    }
}
