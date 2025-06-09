package com.minionslab.core.memory.query.expression;

import com.minionslab.core.common.message.Message;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ContainsKeywordExpression implements MemoryQueryExpression {
    private final String field;
    private final String keyword;
    
    public ContainsKeywordExpression(String field, String keyword) {
        this.field = field;
        this.keyword = keyword;
    }
    

    
    @Override
    public boolean evaluate(Message message) {
        Object fieldValue = message.getFieldValue(field);
        if (fieldValue == null) {
            return false;
        }
        return fieldValue instanceof String && ((String) fieldValue).contains(keyword);
       
    }
}
