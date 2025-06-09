package com.minionslab.core.memory.strategy;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.memory.MemoryOperation;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.MemoryQuery;

import java.util.List;

/**
 * MemoryQueryStrategy defines a pluggable strategy for querying memory in the MCP framework.
 * Strategies can be composed, chained, or selected dynamically to support hybrid or context-aware querying.
 * <p>
 * Implementors can create custom query strategies for different storage backends, policies,
 * or optimization goals. Strategies can be registered and discovered at runtime.
 */
public interface MemoryQueryStrategy<T extends ProcessContext> extends MemoryStrategy<T> {
    
    default List<MemoryOperation> getOperationsSupported() {
        return List.of(MemoryOperation.QUERY);
    }
    
    MemoryQuery getMemoryQuery(T context);
    
    List<MemorySubsystem> getSupportedSubsystem();
    
}
