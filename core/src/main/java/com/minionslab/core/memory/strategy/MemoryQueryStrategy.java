package com.minionslab.core.memory.strategy;

import com.minionslab.core.memory.MemoryOperation;

import java.util.List;

/**
 * MemoryQueryStrategy defines a pluggable strategy for querying memory in the MCP framework.
 * Strategies can be composed, chained, or selected dynamically to support hybrid or context-aware querying.
 * <p>
 * Implementors can create custom query strategies for different storage backends, policies,
 * or optimization goals. Strategies can be registered and discovered at runtime.
 */
public interface MemoryQueryStrategy extends MemoryStrategy{
    
    default List<MemoryOperation> getOperationsSupported() {
        return List.of(MemoryOperation.QUERY);
    }
}
