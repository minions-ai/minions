package com.minionslab.core.memory.strategy;

import com.minionslab.core.memory.MemoryContext;
import com.minionslab.core.memory.MemoryQuery;
import com.minionslab.core.message.Message;

import java.util.List;

/**
 * MemoryQueryStrategy defines a pluggable strategy for querying memory in the MCP framework.
 * Strategies can be composed, chained, or selected dynamically to support hybrid or context-aware querying.
 * <p>
 * Implementors can create custom query strategies for different storage backends, policies,
 * or optimization goals. Strategies can be registered and discovered at runtime.
 */
public interface MemoryQueryStrategy extends MemoryStrategy {
    /**
     * Returns true if this strategy accepts the given context for querying.
     *
     * @param context the memory context
     * @return true if accepted
     */
    boolean accepts(MemoryContext context);
    
    /**
     * Query memory using the given context.
     *
     * @param context the memory context
     * @return the list of messages matching the query
     */
    List<Message> query(MemoryContext context);
}
