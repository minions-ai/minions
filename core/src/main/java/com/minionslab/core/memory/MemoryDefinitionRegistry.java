package com.minionslab.core.memory;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MemoryDefinitionRegistry {
    
    private final @NotNull Map<MemorySubsystem, MemoryDefinition> defintionMap;
    
    private List<MemoryDefinition> definitions;
    
    @Autowired
    public MemoryDefinitionRegistry(List<MemoryDefinition> definitions) {
        defintionMap = definitions.stream().collect(Collectors.toMap(MemoryDefinition::getMemorySubsystem, def -> def));
    }
    
    public MemoryDefinition getMemoryDefinition(MemorySubsystem subsystem) {
        return defintionMap.get(subsystem);
    }
}
