package com.minionslab.core.memory.strategy;

import com.minionslab.core.common.message.Message;
import com.minionslab.core.memory.query.MemoryQuery;

import java.util.List;
import java.util.Map;

public interface PersistenceAdapter {
    void save(Message message);
    
    List<Message> query(MemoryQuery query, Map<String, Object> extraParams);
}
