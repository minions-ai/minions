package com.minionslab.core.message; // Example package

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.function.Supplier;



@Data
@Accessors
@SuperBuilder(toBuilder = true)
public class SimpleMessage extends AbstractMessage {
    
    private String content;
    
    
    @Override
    protected Map<String, Supplier<Object>> populateFieldAccessors() {
        Map<String, Supplier<Object>> supplierMap = super.populateFieldAccessors();
        supplierMap.put("content", this::getContent);
        return supplierMap;
    }
    
    @Override
    public String toPromptString() {
        return "[" + role + "] " + content;
    }
    
    @Override
    public String toString() {
        return id;
    }
    
    
}