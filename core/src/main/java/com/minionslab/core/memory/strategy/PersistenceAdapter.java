package com.minionslab.core.memory.strategy;

import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.message.Message;

import java.util.List;
import java.util.Map;

public interface PersistenceAdapter {
    void save(Message message);
    
    List<Message> query(MemoryQuery query, Map<String, Object> extraParams);
}
