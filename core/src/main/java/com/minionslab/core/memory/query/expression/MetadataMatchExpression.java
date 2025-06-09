package com.minionslab.core.memory.query.expression;

import com.minionslab.core.common.message.Message;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class MetadataMatchExpression implements MemoryQueryExpression {
    private final String key;
    private final Object expectedValue;
    
    public MetadataMatchExpression(String key, Object expectedValue) {
        this.key = key;
        this.expectedValue = expectedValue;
    }
    
    public boolean evaluate(Message message) {
        return expectedValue.equals(message.getMetadata().get(key));
    }
    

}