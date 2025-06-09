package com.minionslab.core.memory.query.expression;

import com.minionslab.core.common.message.Message;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class LogicalExpression implements MemoryQueryExpression {
    private final LogicalOperator operator;
    private final List<MemoryQueryExpression> expressions;
    
    public LogicalExpression(LogicalOperator operator, List<MemoryQueryExpression> expressions) {
        this.operator = operator;
        this.expressions = expressions;
    }
    
    public boolean evaluate(Message message) {
        return switch (operator) {
            case AND -> expressions.stream().allMatch(expr -> expr.evaluate(message));
            case OR -> expressions.stream().anyMatch(expr ->
                                                             expr.evaluate(message));
            case NOT -> expressions.size() == 1 && !expressions.get(0).evaluate(message);
        };
    }
    

}
