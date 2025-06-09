package com.minionslab.core.memory.query.expression;

import com.minionslab.core.common.message.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface  MemoryQueryExpression {
    
    
    boolean evaluate(Message message);
    
    default MemoryQueryExpression and(MemoryQueryExpression... expressions) {
        List<MemoryQueryExpression> all = new ArrayList<>();
        all.add(this); // `this` expression comes first
        Collections.addAll(all, expressions); // add the rest
        return new LogicalExpression(LogicalOperator.AND, all);
    }
    
    default MemoryQueryExpression or(MemoryQueryExpression... expressions) {
        List<MemoryQueryExpression> all = new ArrayList<>();
        all.add(this); // `this` expression comes first
        Collections.addAll(all, expressions); // add the rest
        return new LogicalExpression(LogicalOperator.OR, all);
    }
    
    default MemoryQueryExpression not(MemoryQueryExpression... expressions) {
        List<MemoryQueryExpression> all = new ArrayList<>();
        all.add(this); // `this` expression comes first
        Collections.addAll(all, expressions); // add the rest
        return new LogicalExpression(LogicalOperator.NOT, all);
    }
}
