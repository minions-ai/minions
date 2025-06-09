package com.minionslab.core.memory.strategy.persistence.mongo;

import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.expression.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class MongoQueryTranslator {
    
    /**
     * Translates a full MemoryQuery into a Spring Data MongoDB Query object.
     */
    public Query translate(MemoryQuery query) {
        Criteria criteria = translateExpression(query.getExpression());
        Query mongoQuery = new Query(criteria);
        
        if (query.getLimit() > 0) {
            mongoQuery.limit(query.getLimit());
        }
        
        return mongoQuery;
    }
    
    /**
     * Translates a MemoryQueryExpression into a Spring Criteria object.
     */
    private Criteria translateExpression(MemoryQueryExpression expression) {
        return switch (expression) {
            case FieldEqualsExpression eq -> Criteria.where(eq.field()).is(eq.value());
            case ContainsKeywordExpression contains ->
                    Criteria.where(contains.field()).regex(".*" + java.util.regex.Pattern.quote(contains.keyword()) + ".*");
            case RangeExpression range -> {
                Criteria c = Criteria.where(range.field());
                yield (range.rangeType() == RangeType.AFTER) ? c.gt(range.time()) : c.lt(range.time());
            }
            case MetadataMatchExpression meta ->
                    Criteria.where("metadata." + meta.key()).is(meta.expectedValue());
            case LogicalExpression logical -> translateLogical(logical);
            default -> throw new UnsupportedOperationException("Unsupported expression: " + expression.getClass());
        };
    }
    
    private Criteria translateLogical(LogicalExpression logical) {
        List<Criteria> criteriaList = logical.expressions().stream()
                                             .map(this::translateExpression)
                                             .toList();
        
        return switch (logical.operator()) {
            case AND -> new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
            case OR -> new Criteria().orOperator(criteriaList.toArray(new Criteria[0]));
            case NOT -> new Criteria().norOperator(criteriaList.toArray(new Criteria[0])); // Spring uses nor for NOT
        };
    }
}
