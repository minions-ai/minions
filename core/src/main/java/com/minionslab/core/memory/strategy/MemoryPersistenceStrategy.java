package com.minionslab.core.memory.strategy;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.memory.MemoryContext;
import com.minionslab.core.memory.MemoryOperation;
import com.minionslab.core.memory.query.MemoryQuery;

import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for persisting and retrieving memory items.
 * Implementations of this interface will handle the specifics of interacting
 * with a particular data store (e.g., SQL database, NoSQL database, vector store, in-memory).
 */
public interface MemoryPersistenceStrategy<T extends Message> extends MemoryStrategy{ // Or MemoryPersistenceStrategy if you prefer
    
    /**
     * Saves or updates a single memory item in the persistence layer.
     * If the item has an ID that exists, it's typically an update; otherwise, an insert.
     *
     * @param item The memory item to save.
     * @param <T>  The type of the memory item, extending MemoryItem.
     * @return The saved or updated memory item (e.g., with a generated ID or updated version/timestamp).
     */
    T save(T item);
    
    /**
     * Saves or updates a collection of memory items in the persistence layer.
     * This might be optimized for batch operations by some implementations.
     *
     * @param items The list of memory items to save.
     * @param <T>   The type of the memory items, extending MemoryItem.
     * @return A list of the saved or updated memory items.
     */
    List<T> saveAll(List<T> items);
    
    /**
     * Retrieves a memory item by its unique identifier.
     *
     * @param id       The unique ID of the memory item.
     * @param itemType The class of the memory item to retrieve. This helps in
     * directing the query to the correct store/table/collection and for type casting.
     * @param <T>      The type of the memory item, extending MemoryItem.
     * @return An {@link Optional} containing the memory item if found, otherwise {@link Optional#empty()}.
     */
    Optional<T> findById(String id, Class<T> itemType);
    
    /**
     * Deletes a memory item by its unique identifier.
     *
     * @param id       The unique ID of the memory item to delete.
     * @param itemType The class of the memory item to delete, for targeting the correct store.
     * @param <T>      The type of the memory item, extending MemoryItem.
     * @return true if an item was deleted, false otherwise (optional: some might prefer void).
     */
    boolean deleteById(String id, Class<T> itemType);
    
    /**
     * Deletes all memory items of a specific type. Use with caution.
     *
     * @param itemType The class of memory items to delete.
     * @param <T>      The type of the memory item, extending MemoryItem.
     */
    void deleteAllOfType(Class<T> itemType);
    
    /**
     * Counts the number of memory items of a specific type.
     *
     * @param itemType The class of memory items to count.
     * @param <T>      The type of the memory item.
     * @return The total number of items of the specified type.
     */
    long count(Class<T> itemType);
    
    
    // --- Querying Methods for Specific Types ---
    
    /**
     * Fetches a list of candidate {@link Message} items based on the provided {@link MemoryQuery}.
     * The implementation is responsible for:
     * 1. Interpreting the {@code MemoryQuery}.
     * 2. Translating it into a query suitable for its specific backend (e.g., SQL, MongoDB query, Vector search query).
     * 3. Executing the query against the backend.
     * 4. Mapping the results back to a list of {@code Message} objects.
     * This method should aim to apply as much filtering as possible at the database level for efficiency.
     * Further refinement (like in-memory similarity ranking if not done by DB) might be done by a QueryStrategy.
     *
     * @param query The {@link MemoryQuery} DSL object containing query criteria for messages.
     * @return A list of {@link Message} objects that are candidates matching the query.
     */
    List<T> fetchCandidateMessages(MemoryQuery query);
    
    @Override
    default List<MemoryOperation> getOperationsSupported() {
        return List.of(MemoryOperation.STORE,MemoryOperation.DELETE,MemoryOperation.RETRIEVE);
    }
    
    @Override
    default boolean accepts(ProcessContext processContext) {
        return processContext instanceof MemoryContext && getOperationsSupported().contains(((MemoryContext) processContext).getOperation());
    }
}