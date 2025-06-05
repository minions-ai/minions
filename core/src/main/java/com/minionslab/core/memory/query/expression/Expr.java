package com.minionslab.core.memory.query.expression;

import java.time.Instant;
import java.util.List;

public final class Expr {
    
    private static final MemoryQueryExpression ALWAYS_TRUE = new AlwaysTrueExpression();
    
    private Expr() {} // prevent instantiation
    
    public static MemoryQueryExpression eq(String field, Object value) {
        return new FieldEqualsExpression(field, value);
    }
    
    public static MemoryQueryExpression contains(String field, String keyword) {
        return new ContainsKeywordExpression(field, keyword);
    }
    
    public static MemoryQueryExpression after(String field, Instant time) {
        return new RangeExpression(field, time, RangeType.AFTER);
    }
    
    public static MemoryQueryExpression before(String field, Instant time) {
        return new RangeExpression(field, time, RangeType.BEFORE);
    }
    
    public static MemoryQueryExpression metadata(String key, Object value) {
        return new MetadataMatchExpression(key, value);
    }
    
    public static MemoryQueryExpression vector(float[] embedding, int topK) {
        return new VectorSimilarityExpression(embedding, topK);
    }
    
    public static MemoryQueryExpression and(List<MemoryQueryExpression> expressions) {
        return and(expressions.toArray(new MemoryQueryExpression[0]));
    }
    
    public static MemoryQueryExpression and(MemoryQueryExpression... expressions) {
        return new LogicalExpression(LogicalOperator.AND, List.of(expressions));
    }
    
    public static MemoryQueryExpression or(MemoryQueryExpression... expressions) {
        return new LogicalExpression(LogicalOperator.OR, List.of(expressions));
    }
    
    public static MemoryQueryExpression not(MemoryQueryExpression expression) {
        return new LogicalExpression(LogicalOperator.NOT, List.of(expression));
    }
    
    public static MemoryQueryExpression alwaysTrue() {
        return ALWAYS_TRUE;
    }
    

}
