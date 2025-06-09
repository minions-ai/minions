package com.minionslab.core.memory;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//todo should memory context carry the level of operators, like Agent, Step, Model call?
public abstract class AbstractMemory<T extends ProcessContext, K extends Message> implements Memory<T, K> {
    private static final Logger log = LoggerFactory.getLogger(AbstractMemory.class);
    
    
    private final MemorySubsystem memorySubsystem;
    private final MemoryPersistenceStrategy persistenceStrategy;
    
    
    public AbstractMemory(MemorySubsystem memorySubsystem, MemoryPersistenceStrategy persistenceStrategy) {
        this.memorySubsystem = memorySubsystem;
        this.persistenceStrategy = persistenceStrategy;
        
    }
    
    
    /**
     * Retrieves a message by its unique ID.
     *
     * @param id the message ID
     * @return the message, or null if not found
     */
    @Override
    public K retrieve(String id) {
        log.debug("[{}] Retrieving message with id: {}", memorySubsystem, id);
        Optional<K> result = persistenceStrategy.findById(id, Message.class);
        return result.orElse(null);
    }
    
    /**
     * Processes a memory context operation (store, retrieve, query, delete, flush).
     *
     * @param input the memory context
     * @return the processed context
     */
    @Override
    public ProcessContext process(ProcessContext input) {
        if (input == null || !(input instanceof MemoryContext))
            throw new IllegalArgumentException("MemoryContext cannot be null");
        MemoryContext memoryContext = (MemoryContext) input;
        switch (memoryContext.getOperation()) {
            case STORE:
                log.debug("[{}] Storing messages", memorySubsystem);
                storeAll(memoryContext.getMemoryRequest().getMessagesToStore());
                break;
            case RETRIEVE:
            case QUERY:
                log.debug("[{}] Querying messages", memorySubsystem);
                Instant startedAt = Instant.now();
                List<Message> results = persistenceStrategy.fetchCandidateMessages(memoryContext.getMemoryRequest().getQuery());
                input.getResults().clear();
                if (results != null && !results.isEmpty()) {
                    MemoryResult<Message> result = new MemoryResult<>(
                            getMemorySubsystem().toString(),
                            true,
                            results,
                            null,
                            startedAt,
                            Instant.now()
                    );
                    input.addResult(result);
                }
                break;
            case DELETE:
                log.debug("[{}] Deleting message(s)", memorySubsystem);
                List<String> ids = memoryContext.getMemoryRequest().getMessagesIdsToDelete();
                if (ids != null && !ids.isEmpty()) {
                    ids.forEach(this::deleteById);
                }
                break;
            case FLUSH:
                log.debug("[{}] Flushing memory", memorySubsystem);
                flush();
                break;
            default:
                log.warn("[{}] Unsupported operation: {}", memorySubsystem, memoryContext.getOperation());
        }
        return input;
    }
    
    /**
     * Flushes the memory. Default implementation logs a warning. Subclasses should override.
     */
    @Override
    public void flush() {
        log.warn("[{}] flush() called, but not implemented in this memory type.", memorySubsystem);
    }
    
    /**
     * Gets the memory role (for identification or routing).
     *
     * @return the memory role string
     */
    @Override
    public MemorySubsystem getMemorySubsystem() {
        return memorySubsystem;
    }
    
    /**
     * Stores a list of messages in memory.
     *
     * @param m the list of messages
     */
    @Override
    public void storeAll(List<Message> m) {
        log.debug("[{}] Storing {} messages", memorySubsystem, m != null ? m.size() : 0);
        persistenceStrategy.saveAll(m);
    }
    
    /**
     * Deletes a message by its unique ID.
     *
     * @param id the message ID
     * @return true if deleted, false otherwise
     */
    public boolean deleteById(String id) {
        log.debug("[{}] Deleting message with id: {}", memorySubsystem, id);
        return persistenceStrategy.deleteById(id, Message.class);
    }
    
    /**
     * Takes a snapshot of the current memory state. Not supported by default.
     */
    @Override
    public void snapshot() {
        log.warn("[{}] snapshot() not supported by this memory type.", memorySubsystem);
    }
    
    /**
     * Restores the latest memory snapshot. Not supported by default.
     */
    @Override
    public void restoreLatestSnapshot() {
        log.warn("[{}] restoreLatestSnapshot() not supported by this memory type.", memorySubsystem);
    }
    
    /**
     * Stores a single message in memory.
     *
     * @param message the message to store
     */
    @Override
    public void store(K message) {
        log.debug("[{}] Storing single message", memorySubsystem);
        persistenceStrategy.save(message);
    }
    
    /**
     * Queries memory using a MemoryContext.
     *
     * @param input the memory context
     * @return list of messages
     */
    
    
    /**
     * Queries memory using a MemoryQuery.
     *
     * @param query the memory query
     * @return list of messages
     */
    @Override
    public List<K> query(MemoryQuery query) {
        log.debug("[{}] Querying messages", memorySubsystem);
        Instant startedAt = Instant.now();
        List list = persistenceStrategy.fetchCandidateMessages(query);
        List<K> results = list;
        return results;
    }
    
    /**
     * Determines if this memory accepts the given context. Accepts all by default.
     *
     * @param input the memory context
     * @return true if accepted
     */
    @Override
    public boolean accepts(ProcessContext input) {
        return input instanceof MemoryContext;
    }
}
