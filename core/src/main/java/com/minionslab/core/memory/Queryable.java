package com.minionslab.core.memory;

import com.minionslab.core.message.Message;

import java.util.List;

public interface Queryable {
    List<Message> query(MemoryQuery query);
    List<Message> query(MemoryContext context);
} 