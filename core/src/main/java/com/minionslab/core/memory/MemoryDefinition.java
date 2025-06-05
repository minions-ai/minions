package com.minionslab.core.memory;


import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;
import com.minionslab.core.memory.strategy.MemoryStrategyRegistry;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MemoryDefinition defines the configuration and strategy selection for a memory subsystem
 * in the MCP framework. It specifies which query, persistence, and flush strategies to use,
 * as well as the memory's role and name.
 * <p>
 * Implementors can extend this interface to add custom configuration fields, validation,
 * or advanced strategy selection logic. Definitions are used by factories to construct
 * memory managers and chains.
 */
public interface MemoryDefinition {
    /**
     * Get the memory role (for identification or routing).
     *
     * @return the memory role string
     */
    String getMemoryRole();
    
    /**
     * Build a concrete AbstractMemory instance for this definition.
     *
     * @param registry            the memory strategy registry
     * @param persistenceStrategy the persistence strategy for this memory
     * @return a concrete AbstractMemory instance
     */
    default AbstractMemory buildMemory(
            MemoryStrategyRegistry registry,
            MemoryPersistenceStrategy persistenceStrategy
                                      ) {
        
        // Return a DefaultMemory (concrete subclass of AbstractMemory)
        return new DefaultMemory(getMemorySubsystem(), persistenceStrategy);
    }
    
    /**
     * Get the memory subsystem (for identification or routing).
     *
     * @return the memory subsystem
     */
    MemorySubsystem getMemorySubsystem();
    
    /**
     * Get all strategy names used by this memory (query, persist, flush).
     *
     * @return the list of all strategy names
     */
    default List<String> getAllStrategyNames() {
        return Stream.concat(getQueryStrategies().stream(),
                             Stream.of(getPersistStrategy(), getFlushStrategy()))
                     .collect(Collectors.toList());
    }
    
    /**
     * Get the list of query strategy names.
     *
     * @return the list of query strategy names
     */
    List<String> getQueryStrategies();
    
    /**
     * Get the name of the persistence strategy.
     *
     * @return the persistence strategy name
     */
    String getPersistStrategy();
    
    /**
     * Get the name of the flush strategy.
     *
     * @return the flush strategy name
     */
    String getFlushStrategy();
    
    /**
     * Get the memory name (for registration or lookup).
     *
     * @return the memory name string
     */
    String getMemoryName();
}
