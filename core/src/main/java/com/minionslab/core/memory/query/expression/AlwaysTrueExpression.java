package com.minionslab.core.memory.query.expression;

import com.minionslab.core.common.message.Message;

public class AlwaysTrueExpression implements MemoryQueryExpression {
    public boolean evaluate(Message message) {
        return true;
    }
    
    @Override
    public String toString() {
        return "AlwaysTrue";
    }
}
