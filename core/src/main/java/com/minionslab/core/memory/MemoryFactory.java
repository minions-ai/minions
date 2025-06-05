package com.minionslab.core.memory;

import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;
import com.minionslab.core.memory.strategy.MemoryStrategy;
import com.minionslab.core.memory.strategy.MemoryStrategyRegistry;
import com.minionslab.core.memory.strategy.persistence.inmemory.InMemoryPersistenceStrategy;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * MemoryFactory is responsible for constructing and configuring memory managers and memory chains
 * in the MCP framework. It uses registered {@link MemoryStrategy} and {@link MemoryDefinition}
 * instances to assemble flexible, pluggable memory architectures.
 * <p>
 * This class is designed for extensibility: you can override or extend it to support custom
 * memory types, registration logic, or advanced memory construction patterns. It supports
 * dynamic discovery and registration of memory strategies and definitions.
 */
@Component
public class MemoryFactory {
    /**
     * The registry of available memory strategies.
     */
    private final MemoryStrategyRegistry registry;
    /**
     * The registry of available memory definitions.
     */
    private final MemoryDefinitionRegistry memoryDefinitionRegistry;
    
    /**
     * Constructs a MemoryFactory with the given strategy and definition registries.
     *
     * @param registry the memory strategy registry
     * @param memoryDefinitionRegistry the memory definition registry
     */
    @Autowired
    public MemoryFactory(MemoryStrategyRegistry registry, MemoryDefinitionRegistry memoryDefinitionRegistry) {
        this.registry = registry;
        this.memoryDefinitionRegistry = memoryDefinitionRegistry;
    }
    
    /**
     * Creates a MemoryManager for the given list of memory names, using their definitions.
     *
     * @param memoryNames the list of memory names
     * @return a configured MemoryManager
     * @throws IllegalArgumentException if a memory name is null, blank, or not found
     */
    public MemoryManager createMemories(List<MemorySubsystem> memoryNames) {
        List<MemoryDefinition> definitions = new ArrayList<>();
        for (MemorySubsystem memoryName : memoryNames) {
            if (memoryName == null ) {
                throw new IllegalArgumentException("Memory name is null or blank");
            }
            MemoryDefinition def = memoryDefinitionRegistry.getMemoryDefinition(memoryName);
            if (def == null) {
                throw new IllegalArgumentException("Memory definition not found for name: " + memoryName);
            }
            definitions.add(def);
        }
        return createMemoriesByDefinitions(definitions);
    }
    
    // Create the Memory Chain using strategies from the recipe, filling in missing types with defaults
    public MemoryManager createMemoriesByDefinitions(List<MemoryDefinition> definitions) {
        List<Processor<MemoryContext>> memories = new ArrayList<>();
        
        for (MemoryDefinition definition : definitions) {
            // TODO: Replace with proper registry/lookup by name using definition.getPersistStrategy()
            com.minionslab.core.memory.strategy.MemoryPersistenceStrategy persistenceStrategy = new InMemoryPersistenceStrategy();
            AbstractMemory abstractMemory = definition.buildMemory(registry, persistenceStrategy);
            memories.add(abstractMemory);
        }
        return new MemoryManager(memories);
    }
}
