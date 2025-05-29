package com.minionslab.core.memory.strategy;

import com.minionslab.core.memory.MemoryContext;

/**
 * MemoryFlushStrategy defines a pluggable strategy for flushing memory in the MCP framework.
 * Strategies can be composed, chained, or selected dynamically to support hybrid or context-aware flushing.
 * <p>
 * Implementors can create custom flush strategies for different storage backends, policies,
 * or optimization goals. Strategies can be registered and discovered at runtime.
 */
public interface MemoryFlushStrategy extends MemoryStrategy {
    /**
     * Returns true if this strategy accepts the given context for flushing.
     *
     * @param context the memory context
     * @return true if accepted
     */
    boolean accepts(MemoryContext context);
    
    /**
     * Flush the memory using the given context.
     *
     * @param context the memory context
     */
    void flush(MemoryContext context);
}
