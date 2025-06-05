package com.minionslab.core.memory.strategy.persistence.inmemory;

import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.expression.MemoryQueryExpression;
import com.minionslab.core.memory.strategy.MemoryItem;
import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.SimpleMessage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryPersistenceStrategy implements MemoryPersistenceStrategy<Message> {
    private final Map<String, Message> messageStore = new ConcurrentHashMap<>();
    
    @Override
    public <T extends MemoryItem> List<T> saveAll(List<T> items) {
        if (items == null || items.isEmpty())
            return Collections.emptyList();
        return items.stream().map(this::save).collect(Collectors.toList());
    }
    
    @Override
    public <T extends MemoryItem> T save(T item) {
        if (item instanceof Message) {
            Message msg = (Message) item;
            messageStore.put(msg.getId(), msg);
            return item;
        }
        throw new UnsupportedOperationException("Save not implemented for type: " + item.getClass().getName());
    }
    
    @Override
    public <T extends MemoryItem> Optional<T> findById(String id, Class<T> itemType) {
        if (Message.class.isAssignableFrom(itemType) || SimpleMessage.class.isAssignableFrom(itemType)) {
            Message msg = messageStore.get(id);
            if (msg == null)
                return Optional.empty();
            @SuppressWarnings("unchecked")
            T result = (T) msg;
            return Optional.of(result);
        }
        return Optional.empty();
    }
    
    @Override
    public <T extends MemoryItem> boolean deleteById(String id, Class<T> itemType) {
        if (Message.class.isAssignableFrom(itemType) || SimpleMessage.class.isAssignableFrom(itemType)) {
            return messageStore.remove(id) != null;
        }
        return false;
    }
    
    @Override
    public <T extends MemoryItem> void deleteAllOfType(Class<T> itemType) {
        if (Message.class.isAssignableFrom(itemType) || SimpleMessage.class.isAssignableFrom(itemType)) {
            messageStore.clear();
        }
    }
    
    @Override
    public <T extends MemoryItem> long count(Class<T> itemType) {
        if (Message.class.isAssignableFrom(itemType) || SimpleMessage.class.isAssignableFrom(itemType)) {
            return messageStore.size();
        }
        return 0;
    }
    
    
    @Override
    public List<Message> fetchCandidateMessages(MemoryQuery query) {
        MemoryQueryExpression expression = query.getExpression();
        return messageStore.values().stream()
                           .filter(expression::evaluate)
                           .limit(query.getLimit())
                           .toList();
    }
}