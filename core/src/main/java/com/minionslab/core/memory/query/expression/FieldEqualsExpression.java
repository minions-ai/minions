package com.minionslab.core.memory.query.expression;

import com.minionslab.core.message.Message;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class FieldEqualsExpression implements MemoryQueryExpression {
    private final String field;
    private final Object value;
    
    public FieldEqualsExpression(String field, Object value) {
        this.field = field;
        this.value = value;
    }
    
    
    public boolean evaluate(Message message) {
        Object fieldValue = message.getFieldValue(field);
        if (fieldValue == null) {
            return false;
        }
        return value.equals(fieldValue);
        
    }
}