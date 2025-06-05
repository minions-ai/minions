package com.minionslab.core.memory.query;

import com.minionslab.core.memory.query.expression.Expr;
import com.minionslab.core.memory.query.expression.MemoryQueryExpression;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryBuilder {
    private final List<MemoryQueryExpression> clauses = new ArrayList<>();
    
    public QueryBuilder role(MessageRole role) {
        if (role != null) clauses.add(Expr.eq("role", role));
        return this;
    }
    
    public QueryBuilder scope(MessageScope scope) {
        if (scope != null) clauses.add(Expr.eq("scope", scope));
        return this;
    }
    
    public QueryBuilder keyword(String keyword) {
        if (keyword != null) clauses.add(Expr.contains("content", keyword));
        return this;
    }
    
    public QueryBuilder after(Instant after) {
        if (after != null) clauses.add(Expr.after("timestamp", after));
        return this;
    }
    
    public QueryBuilder before(Instant before) {
        if (before != null) clauses.add(Expr.before("timestamp", before));
        return this;
    }
    
    public QueryBuilder entityType(String entityType) {
        if (entityType != null) clauses.add(Expr.eq("entityType", entityType));
        return this;
    }
    
    public QueryBuilder conversationId(String conversationId) {
        if (conversationId != null) clauses.add(Expr.eq("conversationId", conversationId));
        return this;
    }
    
    public QueryBuilder keyword(Map<String, Object> metadata) {
        if (metadata != null) {
            metadata.forEach((k, v) -> clauses.add(Expr.metadata(k, v)));
        }
        return this;
    }
    
    
    
    public MemoryQueryExpression build() {
        if (clauses.isEmpty()) return Expr.alwaysTrue();
        return clauses.size() == 1 ? clauses.get(0) : Expr.and(clauses);
    }
    
    public QueryBuilder id(String id) {
        if (id != null) clauses.add(Expr.eq("id", id));
        return this;
    }
}

