package com.minionslab.core.memory.query.expression;

import com.minionslab.core.message.Message;

public interface  MemoryQueryExpression {
    
    
    boolean evaluate(Message message);
}
