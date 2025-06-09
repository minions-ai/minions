package com.minionslab.core.memory.strategy.query.step;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.QueryConfig;
import com.minionslab.core.memory.query.expression.ExprUtil;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class StepShortTermQueryStrategy implements MemoryQueryStrategy<StepContext> {
    
    
    @Override
    public MemoryQuery getMemoryQuery(StepContext context) {
        QueryConfig queryConfig = StepContext.getConfig().getQueryConfig();
        return MemoryQuery.builder().
                          limit(queryConfig.getLimit())
                          .expression(ExprUtil.getAssistantMessagesExpression(context.getConversationId()))
                          .build();
    }
    
    @Override
    public List<MemorySubsystem> getSupportedSubsystem() {
        return List.of(MemorySubsystem.SHORT_TERM);
    }
    
    @Override
    public boolean accepts(ProcessContext processContext) {
        return processContext != null && processContext instanceof StepContext;
    }
}
