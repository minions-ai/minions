package com.minionslab.persistence.mongo;

import com.minionslab.core.memory.MemoryQuery;
import com.minionslab.core.memory.strategy.PersistenceAdapter;
import com.minionslab.core.message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MongoMemoryPersistanceStrategy implements PersistenceAdapter {
    
    private final MongoTemplate mongoTemplate;
    
    
    @Override
    public void save(Message message) {
        mongoTemplate.save(message, "messages");
    }
    
    @Override
    public List<Message> query(MemoryQuery query, Map<String, Object> extraParams) {
        Query mongoQuery = new Query();
        
        // Role
        if (query.getRole() != null) {
            mongoQuery.addCriteria(Criteria.where("role").is(query.getRole().name()));
        }
        // Scope
        if (query.getScope() != null) {
            mongoQuery.addCriteria(Criteria.where("scope").is(query.getScope().name()));
        }
        // Keyword (search in content)
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            mongoQuery.addCriteria(Criteria.where("content").regex(query.getKeyword(), "i"));
        }
        // Metadata (all key-value pairs must match)
        if (query.getMetadata() != null && !query.getMetadata().isEmpty()) {
            for (Map.Entry<String, Object> entry : query.getMetadata().entrySet()) {
                mongoQuery.addCriteria(Criteria.where("metadata." + entry.getKey()).is(entry.getValue()));
            }
        }
        // Time range
        if (query.getAfter() != null || query.getBefore() != null) {
            Criteria timeCriteria = Criteria.where("timestamp");
            if (query.getAfter() != null) {
                timeCriteria = timeCriteria.gte(query.getAfter());
            }
            if (query.getBefore() != null) {
                timeCriteria = timeCriteria.lte(query.getBefore());
            }
            mongoQuery.addCriteria(timeCriteria);
        }
        // Entity type (if stored in metadata)
        if (query.getEntityType() != null) {
            mongoQuery.addCriteria(Criteria.where("metadata.entityType").is(query.getEntityType()));
        }
        // Limit
        if (query.getLimit() > 0) {
            mongoQuery.limit(query.getLimit());
        }
        // Sort by timestamp descending (most recent first)
        mongoQuery.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        
        // Execute query
        List<Message> results = mongoTemplate.find(mongoQuery, Message.class, "messages");
        return results;
    }
}