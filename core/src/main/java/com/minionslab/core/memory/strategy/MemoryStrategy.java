package com.minionslab.core.memory.strategy;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.MemoryOperation;

import java.util.List;

/**
 * MemoryStrategy defines a pluggable strategy for handling memory operations
 * (store, retrieve, query, flush, etc.) in the MCP framework. Strategies can be
 * composed, chained, or selected dynamically to support hybrid or context-aware memory.
 * <p>
 * Implementors can create custom strategies for different storage backends, policies,
 * or optimization goals. Strategies can be registered and discovered at runtime.
 */
public interface MemoryStrategy<T extends ProcessContext> {
    
    /**
     * Get the list of memory operations supported by this strategy.
     *
     * @return the list of supported operations
     */
    List<MemoryOperation> getOperationsSupported();
    
     boolean accepts(ProcessContext processContext);
    
} 