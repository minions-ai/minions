package com.minionslab.core.memory;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.expression.Expr;
import com.minionslab.core.memory.strategy.MemoryQueryStrategy;
import jdk.jshell.spi.ExecutionControl;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
public class MemoryManager implements Memory {
    /**
     * The list of memory processors (strategies) in the chain.
     */
    private final List<Memory<MemoryContext, Message>> memories;
    private final List<MemoryQueryStrategy> queryStrategies;
    
    /**
     * Constructs a MemoryManager with the given list of memory processors.
     *
     * @param memories the list of memory processors (strategies)
     */
    public MemoryManager(List<Memory<MemoryContext, Message>> memories, List<MemoryQueryStrategy> strategies) {
        this.memories = memories;
        this.queryStrategies = strategies;
    }
    
    /**
     * Retrieves a message by ID by delegating to the memory chain.
     *
     * @param id the message ID
     * @return the retrieved message, or null if not found
     */
    @Override
    public Message retrieve(String id) {
        MemoryQuery query = MemoryQuery.builder().expression(Expr.eq("id", id)).build();
        List<Message> list = memories.stream().map(memory -> memory.retrieve(id)).filter(Objects::nonNull).toList();
        return list.stream().findFirst().orElse(null);
    }
    
    /**
     * Flushes all memory processors in the chain.
     */
    @Override
    public void flush() {
        memories.stream().forEach(memory -> memory.flush());
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
        throw new RuntimeException("This method is not implemented. use store(MemorySubsystem,Message) instead.");
    }
    
    public void store(MemorySubsystem memorySubsystem, Message message) {
        for (Memory<MemoryContext, Message> memory : memories) {
            if (memory.getMemorySubsystem().equals(memorySubsystem)) {
                memory.store(message);
            }
        }
    }
    
    @Override
    public List<Message> query(MemoryQuery query) {
        return execute(query);
    }
    
    private List<Message> execute(MemoryQuery query) {
        return memories.stream()
                       .flatMap(memory -> memory.query(query).stream())
                       .toList();
    }
    
    public List<Message> query(ProcessContext context) {
        List<Message> consolidatedResults = new ArrayList<>();
        for (MemoryQueryStrategy strategy : queryStrategies) {
            if (strategy.getOperationsSupported().contains(MemoryOperation.QUERY) && strategy.getSupportedSubsystem().contains(MemorySubsystem.MEMORY_MANAGER)) {
                MemoryQuery query = strategy.getMemoryQuery(context);
                List<Message> results = execute(query);
                consolidatedResults.addAll(results);
            }
        }
        return consolidatedResults;
    }
    
    @Override
    public boolean accepts(ProcessContext input) {
        return input != null;
    }
    
    //todo needs implementation
    public ProcessContext process(ProcessContext input) {
        return input;
    }
    
    
    public void storeAll(List<Message> messages, MemorySubsystem memorySubsystem) {
        this.memories.stream().filter(memory -> memory.getMemorySubsystem().equals(memorySubsystem))
                     .forEach(memory -> {
                         memory.storeAll(messages);
                     });
    }
}
