package com.minionslab.core.memory.query;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class QueryConfig {
    private final Map<String, String> properties = new HashMap<>();
    private int limit;
    
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }
    
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }
    
    
}
