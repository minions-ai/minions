package com.minionslab.core.memory.strategy.persistence.postgres;

import com.minionslab.core.memory.query.expression.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostgresQueryTranslator {
    
    public static class SqlQuery {
        private final String whereClause;
        private final List<Object> parameters;
        
        public SqlQuery(String whereClause, List<Object> parameters) {
            this.whereClause = whereClause;
            this.parameters = parameters;
        }
        
        public String getWhereClause() {
            return whereClause;
        }
        
        public List<Object> getParameters() {
            return parameters;
        }
        
        @Override
        public String toString() {
            return "WHERE " + whereClause + " | PARAMS: " + parameters;
        }
    }
    
    public SqlQuery translate(MemoryQueryExpression expression) {
        List<Object> params = new ArrayList<>();
        String where = build(expression, params);
        return new SqlQuery(where, params);
    }
    
    private String build(MemoryQueryExpression expr, List<Object> params) {
        return switch (expr) {
            case FieldEqualsExpression eq -> {
                params.add(eq.value());
                yield eq.field() + " = ?";
            }
            case ContainsKeywordExpression contains -> {
                params.add("%" + contains.keyword() + "%");
                yield contains.field() + " ILIKE ?";
            }
            case RangeExpression range -> {
                String op = switch (range.rangeType()) {
                    case AFTER -> ">";
                    case BEFORE -> "<";
                };
                params.add(range.time());
                yield range.field() + " " + op + " ?";
            }
            case MetadataMatchExpression meta -> {
                params.add(meta.expectedValue());
                yield "metadata ->> ? = ?";
            }
            case LogicalExpression logical -> buildLogical(logical, params);
            case AlwaysTrueExpression ignored -> "TRUE";
            default -> throw new UnsupportedOperationException("Unsupported expression: " + expr.getClass());
        };
    }
    
    private String buildLogical(LogicalExpression logical, List<Object> params) {
        List<String> parts = logical.expressions().stream()
                                    .map(expr -> "(" + build(expr, params) + ")")
                                    .toList();
        
        return switch (logical.operator()) {
            case AND -> String.join(" AND ", parts);
            case OR -> String.join(" OR ", parts);
            case NOT -> "NOT " + parts.get(0);
        };
    }
}
