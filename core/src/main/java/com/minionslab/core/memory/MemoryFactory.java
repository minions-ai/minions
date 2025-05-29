package com.minionslab.core.memory;

import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;
import com.minionslab.core.memory.strategy.MemoryStrategy;
import com.minionslab.core.memory.strategy.MemoryStrategyRegistry;
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
    public MemoryManager createMemories(List<String> memoryNames) {
        List<MemoryDefinition> definitions = new ArrayList<>();
        for (String memoryName : memoryNames) {
            if (memoryName == null || memoryName.isBlank()) {
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
            List<MemoryQueryStrategy> queryRecords = new ArrayList<>();
            DefaultMemory defaultMemory = buildMemoryFromDefinition(definition, queryRecords);
            memories.add(defaultMemory);
        }
        return new MemoryManager(memories);
    }
    
    private @NotNull DefaultMemory buildMemoryFromDefinition(@NotNull MemoryDefinition definition, List<MemoryQueryStrategy> queryRecords) {
        List<MemoryStrategy> definitionStrategies = new ArrayList<>(queryRecords);
        
        for (String strategyName : definition.getAllStrategyNames()) {
            if (strategyName == null || strategyName.isBlank()) {
                throw new IllegalArgumentException("Strategy name in definition '" + definition.getMemoryName() + "' is null or blank");
            }
            MemoryStrategy strategy = registry.getByName(strategyName);
            if (strategy == null) {
                throw new IllegalArgumentException("Strategy '" + strategyName + "' not found in registry for memory definition '" + definition.getMemoryName() + "'");
            }
            definitionStrategies.add(strategy);
        }
        // Ensure all MemoryOperation values are covered
        Set<MemoryOperation> presentOps = definitionStrategies.stream()
                                                              .flatMap(s -> s.getOperationsSupported().stream())
                                                              .collect(java.util.stream.Collectors.toSet());
        Set<MemoryOperation> allOps = EnumSet.allOf(MemoryOperation.class);
        allOps.removeAll(presentOps);
        for (MemoryOperation missingOp : allOps) {
            MemoryStrategy defaultStrategy = registry.getDefaultForOperation(missingOp);
            if (defaultStrategy != null) {
                definitionStrategies.add(defaultStrategy);
            } else {
                throw new IllegalStateException("No strategy found for required operation: " + missingOp);
            }
        }
        
        EnumSet<MemoryOperation> supportedOps = EnumSet.noneOf(MemoryOperation.class);
        List<MemoryStrategy> fallbackStrategies = new ArrayList<>();
        supportedOps.forEach(op -> fallbackStrategies.add(registry.getDefaultForOperation(op)));
        
        
        return new DefaultMemory(definition.getMemoryRole(), definitionStrategies, fallbackStrategies);
    }
}
