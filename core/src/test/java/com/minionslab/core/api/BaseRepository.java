package com.minionslab.core.api;

import java.util.Optional;

/**
 * Base repository interface for common database operations.
 * Provides basic CRUD operations that can be extended by specific repositories.
 */
public interface BaseRepository<T, ID> {
    /**
     * Saves an entity to the database
     */
    T save(T entity);

    /**
     * Finds an entity by its ID
     */
    Optional<T> findById(ID id);

    /**
     * Deletes an entity by its ID
     */
    void deleteById(ID id);

    /**
     * Checks if an entity exists by its ID
     */
    boolean existsById(ID id);
} 