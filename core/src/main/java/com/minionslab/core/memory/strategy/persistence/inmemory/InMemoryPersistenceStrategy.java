package com.minionslab.core.memory.strategy.persistence.inmemory;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.common.message.SimpleMessage;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.expression.MemoryQueryExpression;
import com.minionslab.core.memory.strategy.MemoryItem;
import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryPersistenceStrategy implements MemoryPersistenceStrategy<Message> {
    private final Map<String, Message> messageStore = new ConcurrentHashMap<>();
    
    @Override
    public  List<Message> saveAll(List<Message> items) {
        if (items == null || items.isEmpty())
            return Collections.emptyList();
        return items.stream().map(this::save).collect(Collectors.toList());
    }
    
    @Override
    public  Message save(Message item) {
        if (item instanceof Message msg) {
            messageStore.put(msg.getId(), msg);
            return item;
        }
        throw new UnsupportedOperationException("Save not implemented for type: " + item.getClass().getName());
    }
    
    @Override
    public  Optional<Message> findById(String id, Class<Message> itemType) {
        if (Message.class.isAssignableFrom(itemType) || SimpleMessage.class.isAssignableFrom(itemType)) {
            Message msg = messageStore.get(id);
            if (msg == null)
                return Optional.empty();
            @SuppressWarnings("unchecked")
            Message result = (Message) msg;
            return Optional.of(result);
        }
        return Optional.empty();
    }
    
    @Override
    public  boolean deleteById(String id, Class<Message> itemType) {
        if (Message.class.isAssignableFrom(itemType) || SimpleMessage.class.isAssignableFrom(itemType)) {
            return messageStore.remove(id) != null;
        }
        return false;
    }
    
    @Override
    public  void deleteAllOfType(Class<Message> itemType) {
        if (Message.class.isAssignableFrom(itemType) || SimpleMessage.class.isAssignableFrom(itemType)) {
            messageStore.clear();
        }
    }
    
    @Override
    public  long count(Class<Message> itemType) {
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