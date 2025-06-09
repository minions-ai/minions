package com.minionslab.core.memory;


import com.minionslab.core.memory.strategy.MemoryFlushStrategy;
import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;

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
    default AbstractMemory buildMemory() {
        // Return a DefaultMemory (concrete subclass of AbstractMemory)
        return new DefaultMemory(getMemorySubsystem(), getPersistStrategy());
    }
    
    /**
     * Get the memory subsystem (for identification or routing).
     *
     * @return the memory subsystem
     */
    MemorySubsystem getMemorySubsystem();
    

    
    /**
     * Get the name of the persistence strategy.
     *
     * @return the persistence strategy name
     */
    MemoryPersistenceStrategy getPersistStrategy();
    

    
    /**
     * Get the name of the flush strategy.
     *
     * @return the flush strategy name
     */
    MemoryFlushStrategy getFlushStrategy();
    
    /**
     * Get the memory name (for registration or lookup).
     *
     * @return the memory name string
     */
    String getMemoryName();
}
