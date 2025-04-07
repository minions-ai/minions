package com.minionslab.api.test.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A context class for managing test entities.
 * Provides a type-safe way to store and retrieve test data.
 */
public class TestContext {
    private final Map<Class<?>, Object> entities = new HashMap<>();

    /**
     * Stores an entity in the context
     */
    public <T> void put(Class<T> type, T entity) {
        entities.put(type, entity);
    }

    /**
     * Retrieves an entity from the context
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        return (T) entities.get(type);
    }

    /**
     * Removes an entity from the context
     */
    public <T> void remove(Class<T> type) {
        entities.remove(type);
    }

    /**
     * Checks if an entity exists in the context
     */
    public <T> boolean contains(Class<T> type) {
        return entities.containsKey(type);
    }

    /**
     * Clears all entities from the context
     */
    public void clear() {
        entities.clear();
    }
} 