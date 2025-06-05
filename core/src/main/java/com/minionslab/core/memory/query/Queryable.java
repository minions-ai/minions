package com.minionslab.core.memory.query;

import com.minionslab.core.memory.MemoryContext;
import com.minionslab.core.message.Message;

import java.util.List;

public interface Queryable<T extends Message> {
    List<T> query(MemoryQuery query);
    List<T> query(MemoryContext context);
} 