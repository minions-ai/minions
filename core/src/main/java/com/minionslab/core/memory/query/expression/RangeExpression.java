package com.minionslab.core.memory.query.expression;

import com.minionslab.core.message.Message;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true, fluent = true)
public class RangeExpression implements MemoryQueryExpression {
    private final String field;
    private final Instant time;
    private final RangeType rangeType;
    
    public RangeExpression(String field, Instant time, RangeType rangeType) {
        this.field = field;
        this.time = time;
        this.rangeType = rangeType;
    }
    
    
    public boolean evaluate(Message message) {
        Object fieldValue = message.getFieldValue(field);
        if (fieldValue == null) {
            return false;
        }
        if (!(fieldValue instanceof Instant)) return false;
        Instant timestamp = (Instant) fieldValue;
        return (rangeType == RangeType.AFTER && timestamp.isAfter(time)) ||
                       (rangeType == RangeType.BEFORE && timestamp.isBefore(time));
       
    }
}
