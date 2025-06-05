package com.minionslab.core.memory.strategy.query;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.QueryBuilder;
import com.minionslab.core.memory.query.QueryConfig;
import com.minionslab.core.message.MessageRole;

public class AgentSmartMemoryQueryStrategy extends AbstractSmartMemoryQueryStrategy<AgentContext> {
    
    
    @Override
    public MemoryQuery getLongTermQuery(AgentContext agentContext) {
        
        return null;
    }
    
    @Override
    public MemoryQuery getEpisodicQuery(AgentContext agentContext) {
        return null;
    }
    
    @Override
    public MemoryQuery getEntityQuery(AgentContext agentContext) {
        return null;
    }
    
    @Override
    public MemoryQuery getVectorQuery(AgentContext agentContext) {
        return null;
    }
    
    @Override
    public MemoryQuery getShortTermQuery(AgentContext agentContext) {
        QueryConfig queryConfig = agentContext.getConfig().getQueryConfig();
        
        MemoryQuery query = MemoryQuery.builder().
                                       limit(Integer.parseInt(queryConfig.getLimit()))
                                       .expression(new QueryBuilder().role(MessageRole.ASSISTANT).conversationId(agentContext.getAgent().getAgentId()).build())
                                       .build();
        return query;
    }
}
