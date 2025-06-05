package com.minionslab.core.memory.strategy;



import java.time.Instant;
import java.util.Map;

/**
 * Represents a base item that can be stored in the agent's memory system.
 * All specific types of memory (e.g., Message, Fact, Observation) should
 * implement this interface.
 */
public interface MemoryItem {
    
    /**
     * Gets the unique identifier for this memory item.
     *
     * @return The unique ID string.
     */
    String getId();
    

    
    /**
     * Gets the timestamp associated with this memory item.
     * This could represent creation time, last update time, or the time of an event.
     *
     * @return The {@link Instant} representing the timestamp.
     */
    Instant getTimestamp();
    

    
    /**
     * Gets the metadata associated with this memory item.
     * Metadata can store various auxiliary information like source, tags, relevance scores,
     * agent ID, session ID, etc.
     *
     * @return A {@link Map} where keys are metadata field names and values are the metadata values.
     * Implementations should ideally return a mutable map or provide methods to modify metadata.
     * If an immutable map is returned by an implementation, it should be clearly documented.
     */
    Map<String, Object> getMetadata();
    

    
    /**
     * Gets a specific metadata value by its key.
     *
     * @param key The key of the metadata entry.
     * @return The metadata value, or null if the key is not found.
     */
    default Object getMetadata(String key) {
        return getMetadata().get(key);
    }
    
    /**
     * Puts or updates a specific metadata entry.
     *
     * @param key   The key of the metadata entry.
     * @param value The value of the metadata entry.
     */
    default void putMetadata(String key, Object value) {
        getMetadata().put(key, value);
    }
}
