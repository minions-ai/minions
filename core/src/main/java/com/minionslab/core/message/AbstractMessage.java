package com.minionslab.core.message;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Data
@Accessors
@SuperBuilder(toBuilder = true)
public abstract class AbstractMessage implements Message {
    
    protected String id;
    protected Instant timestamp;
    protected MessageRole role;
    protected MessageScope scope;
    /*
     * This is a map of getter suppliers. If you want to be able to query messages by their static fields then they need to be added to this map otherwise they won't be visible
     * to the query executor
     * */
    protected int tokenCount;
    private Map<String, Object> metadata = new HashMap<>();
    protected final Map<String, Supplier<Object>> fieldAccessors = populateFieldAccessors();
    
    
    protected AbstractMessage() {
    
    }
    
    protected Map<String, Supplier<Object>> populateFieldAccessors() {
        Map<String, Supplier<Object>> accessors = new HashMap<>();
        accessors.putAll(Map.of(
                "id", this::getId,
                "content", this::getContent,
                "role", this::getRole,
                "scope", this::getScope,
                "tokenCount", this::getTokenCount,
                "metadata", this::getMetadata));
        return accessors;
    }
    
    @Override
    public Object getFieldValue(String field) {
        Supplier<Object> supplier = this.getFieldAccessors().get(field);
        if (supplier != null)
            return supplier.get();
        return null;
    }
    
    
}
