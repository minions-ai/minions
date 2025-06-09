package com.minionslab.core.memory.query;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.memory.MemoryContext;

import java.util.List;

public interface Queryable<T extends Message> {
    List<T> query(MemoryQuery query);

} 