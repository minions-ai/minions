package com.minionslab.core.memory;

import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.QueryBuilder;
import com.minionslab.core.memory.query.expression.Expr;
import com.minionslab.core.message.Message;

import java.util.Collections;
import java.util.List;

/**
 * MemoryManager coordinates memory operations (store, retrieve, flush, etc.)
 * by delegating to a chain of {@link Processor} instances. Each processor can
 * implement a different memory strategy (short-term, long-term, vector, etc.),
 * allowing for flexible, pluggable, and extensible memory management.
 * <p>
 * The chain-of-responsibility design enables custom memory strategies, ordering,
 * and composition. You can extend this class or provide custom processors to
 * support new memory types, hybrid memory, or advanced behaviors.
 */
public class MemoryManager implements Memory, Processor<MemoryContext> {
    /**
     * The list of memory processors (strategies) in the chain.
     */
    private final List<Processor<MemoryContext>> memories;
    
    /**
     * Constructs a MemoryManager with the given list of memory processors.
     *
     * @param memories the list of memory processors (strategies)
     */
    public MemoryManager(List<Processor<MemoryContext>> memories) {
        this.memories = memories;
    }
    
    /**
     * Retrieves a message by ID by delegating to the memory chain.
     *
     * @param id the message ID
     * @return the retrieved message, or null if not found
     */
    @Override
    public Message retrieve(String id) {
        MemoryContext context = new MemoryContext();
        context.setOperation(MemoryOperation.RETRIEVE);
        MemoryQuery query = MemoryQuery.builder().expression(new QueryBuilder().id(id).build()).build();
        context.setMemoryRequest(new MemoryRequest(query, null));
        MemoryContext finalContext = context;
        memories.stream().forEach(memory -> memory.process(finalContext));
        List<Message> messages = context.getMemoryRequest() != null && context.getMemoryRequest().getMessagesToStore() != null
                                         ? context.getMemoryRequest().getMessagesToStore() : Collections.emptyList();
        if (!messages.isEmpty()) {
            return messages.get(0);
        }
        return null;
    }
    
    /**
     * Flushes all memory processors in the chain.
     */
    @Override
    public void flush() {
        MemoryContext context = new MemoryContext();
        context.setOperation(MemoryOperation.FLUSH);
        memories.stream().forEach(memory -> memory.process(context));
    }
    
    /**
     * Takes a snapshot of the current memory state. (Not implemented)
     */
    @Override
    public void snapshot() {
    
    }
    
    /**
     * Restores the latest memory snapshot. (Not implemented)
     */
    @Override
    public void restoreLatestSnapshot() {
    
    }
    
    /**
     * Returns the role of this memory manager (for identification).
     *
     * @return the memory role string
     */
    @Override
    public MemorySubsystem getMemorySubsystem() {
        return MemorySubsystem.MEMORY_MANAGER;
    }
    
    /**
     * Stores all messages in the memory chain.
     *
     * @param m the list of messages to store
     */
    @Override
    public void storeAll(List m) {
        m.forEach(message -> store((Message) message));
    }
    
    @Override
    public void store(Message message) {
        MemoryContext context = new MemoryContext();
        context.setOperation(MemoryOperation.STORE);
        context.setMemoryRequest(new MemoryRequest(null, List.of(message)));
        memories.stream().forEach(memory -> memory.process(context));
    }
    
    @Override
    public List<Message> query(MemoryQuery query) {
        return execute(query);
    }
    
    private List<Message> execute(MemoryQuery query) {
        MemoryContext context = new MemoryContext();
        context.setOperation(MemoryOperation.RETRIEVE);
        context.setMemoryRequest(new MemoryRequest(query, null));
        memories.stream().forEach(memory -> memory.process(context));
        
        return List.of();
    }
    
    @Override
    public List<Message> query(MemoryContext context) {
        return List.of();
    }
    
    @Override
    public boolean accepts(MemoryContext input) {
        return true;
    }
    
    @Override
    public MemoryContext process(MemoryContext input) {
        return input;
    }
    
    
}
