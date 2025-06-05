package com.minionslab.core.memory.definitions;

import com.minionslab.core.memory.MemoryDefinition;
import com.minionslab.core.memory.MemorySubsystem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShortTermMemory implements MemoryDefinition {
    @Override
    public List<String> getQueryStrategies() {
        return List.of();
    }
    
    @Override
    public String getPersistStrategy() {
        return "in_memory";
    }
    
    @Override
    public String getFlushStrategy() {
        return "";
    }
    
    @Override
    public String getMemoryRole() {
        return MemorySubsystem.SHORT_TERM.name();
    }
    
    @Override
    public MemorySubsystem getMemorySubsystem() {
        return MemorySubsystem.SHORT_TERM;
    }
    
    @Override
    public String getMemoryName() {
        return "";
    }
}
