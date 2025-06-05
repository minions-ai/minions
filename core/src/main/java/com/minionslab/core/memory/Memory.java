package com.minionslab.core.memory;

import com.minionslab.core.memory.query.Queryable;
import com.minionslab.core.message.Message;

import java.util.List;

/**
 * Memory defines the contract for a memory subsystem in the MCP framework.
 * Implementations may represent short-term, long-term, vector, or hybrid memory,
 * and can be composed, extended, or chained for advanced behaviors.
 * <p>
 * Memory implementations can support pluggable strategies for storage, retrieval,
 * flushing, and querying, enabling flexible and extensible memory architectures.
 *
 * @param <T> the type of Message handled by this memory
 */
public interface Memory<T extends Message> extends Queryable<T> {
    /**
     * Store a single message in memory.
     *
     * @param message the message to store
     */
    void store(T message);
    /**
     * Retrieve a message by ID.
     *
     * @param id the message ID
     * @return the retrieved message, or null if not found
     */
    T retrieve(String id);
    /**
     * Flush the memory, clearing or persisting its contents as appropriate.
     */
    void flush();
    /**
     * Take a snapshot of the current memory state (for persistence or rollback).
     */
    void snapshot();
    /**
     * Restore the latest memory snapshot.
     */
    void restoreLatestSnapshot();
    /**
     * Get the role of this memory (for identification or routing).
     *
     * @return the memory role string
     */
    MemorySubsystem getMemorySubsystem();
    /**
     * Store a list of messages in memory.
     *
     * @param m the list of messages to store
     */
    void storeAll(List<Message> m);
    /**
     * Record type for memory query metadata.
     */
    record MemoryQueryRecord(String memoryRole, String initiatorType, String strategyByName) {
    }
}