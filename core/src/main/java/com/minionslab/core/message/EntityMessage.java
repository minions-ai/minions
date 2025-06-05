package com.minionslab.core.message;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.function.Supplier;

@Data
@Accessors
@SuperBuilder
public class EntityMessage extends SimpleMessage {
    
    
    /**
     * The entity extracted or created by the LLM. Can be a Map or a POJO.
     */
    private Object entity;
    
    

    
    @Override
    public String toPromptString() {
        return String.format("[ENTITY][%s] %s | Entity: %s", getRole(), this.getContent(), entity != null ? entity.toString() : "null");
    }
    
    
    @Override
    protected Map<String, Supplier<Object>> populateFieldAccessors() {
        Map<String, Supplier<Object>> supplierMap = super.populateFieldAccessors();
        supplierMap.put("entity", this::getEntity);
        return supplierMap;
    }
}
