package com.minionslab.core.memory;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DefaultMemoryDefintion implements MemoryDefinition {
    private List<String> queryStrategies;
    private String persistenceStrategy;
    private String flushStrategy;
    private String name;
    private String memoryRole;
    
    @Override
    public List<String> getQueryStrategies() {
        return queryStrategies;
    }
    
    @Override
    public String getPersistStrategy() {
        return persistenceStrategy;
    }
    
    @Override
    public String getFlushStrategy() {
        return flushStrategy;
    }
    
    @Override
    public String getMemoryRole() {
        return memoryRole;
    }
    
    @Override
    public String getMemoryName() {
        return this.name;
    }
    
    
}
