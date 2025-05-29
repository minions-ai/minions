package com.minionslab.core.memory;

import com.minionslab.core.common.chain.Chain;
import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.memory.strategy.MemoryStrategy;
import com.minionslab.core.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

//todo should memory context carry the level of operators, like Agent, Step, Model call?
public class DefaultMemory implements Memory<Message>, Processor<MemoryContext> {
    private static final Logger log = LoggerFactory.getLogger(DefaultMemory.class);
    
    
    private final String memoryRole;
    private final Chain<Processor, MemoryContext> strategyChain;
    
    public DefaultMemory(String memoryRole, List<MemoryStrategy> strategies, List<MemoryStrategy> fallbackStrategies) {
        this.memoryRole = memoryRole;
        this.strategyChain = buildStrategyChain(strategies, fallbackStrategies);
    }
    
    
    //todo correct this implementation
    private Chain<Processor, MemoryContext> buildStrategyChain(List<MemoryStrategy> strategies, List<MemoryStrategy> fallbackStrategies) {
        return null;
    }
    
    @Override
    public Message retrieve(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Message id cannot be null or blank");
        MemoryContext context = new MemoryContext();
        
        context.setOperation(MemoryOperation.RETRIEVE);
        context.getMemoryRequest().setQuery(MemoryQuery.builder().messageId(id).build());
        context = process(context);
        List<Message> retrievedMessage = context.getResults().stream().flatMap(result -> result.getResults().stream()).collect(Collectors.toUnmodifiableList());
        if (retrievedMessage.isEmpty()) {
            throw new MessageNotFoundException(String.format("Message with id %s was not found", id));
        }
        return retrievedMessage.getFirst();
    }
    
    @Override
    public MemoryContext process(MemoryContext input) {
        if (input == null)
            throw new IllegalArgumentException("MemoryContext cannot be null");
        input = strategyChain.process(input);
        return input;
    }
    
    @Override
    public void flush() {
        MemoryContext context = new MemoryContext();
        context.setOperation(MemoryOperation.FLUSH);
        process(context);
    }
    
    @Override
    public void snapshot() {
        // Placeholder: implement snapshot logic if/when persistence is available
        log.info("snapshot() called, but not implemented");
    }
    
    @Override
    public void restoreLatestSnapshot() {
        // Placeholder: implement restore logic if/when persistence is available
        log.info("restoreLatestSnapshot() called, but not implemented");
    }
    
    @Override
    public String getMemoryRole() {
        return memoryRole;
    }
    
    @Override
    public void storeAll(List<Message> m) {
        m.forEach(message -> store((Message) message));
    }
    
    @Override
    public void store(Message message) {
        if (message == null)
            throw new IllegalArgumentException("Message cannot be null");
        MemoryContext context = new MemoryContext();
        context.getMemoryRequest().setMessagesToStore(List.of(message));
        context.setOperation(MemoryOperation.STORE);
        process(context);
    }
    
    @Override
    public List<Message> query(MemoryContext input) {
        if (input == null)
            throw new IllegalArgumentException("MemoryContext cannot be null");
        input.setOperation(MemoryOperation.QUERY);
        MemoryQuery query = input.getMemoryRequest().getQuery();
        return this.query(query);
    }
    
    @Override
    public List<Message> query(MemoryQuery input) {
        if (input == null)
            throw new IllegalArgumentException("MemoryQuery cannot be null");
        return executeQuery(input);
    }
    
    private List<Message> executeQuery(MemoryQuery memoryQuery) {
        MemoryContext context = new MemoryContext();
        context.setOperation(MemoryOperation.QUERY);
        context.getMemoryRequest().setQuery(memoryQuery);
        context = process(context);
        return context.getResults().stream().flatMap(result -> result.getResults().stream()).collect(Collectors.toUnmodifiableList());
    }
    
    @Override
    public boolean accepts(MemoryContext input) {
        // Accept all for now, or add logic as needed
        return true;
    }
}
