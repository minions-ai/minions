package com.minionslab.core.memory;

import com.minionslab.core.message.Message;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Represents a request to the memory system, including messages to store and/or a query for retrieval.
 */
@Data
@Accessors(chain = true)
public class MemoryRequest {
    private MemoryQuery query;
    private List<Message> messagesToStore;
    
    public MemoryRequest() {
    }
    
    public MemoryRequest(MemoryQuery query, List<Message> messagesToStore) {
        this.query = query;
        this.messagesToStore = messagesToStore;
    }
    

    
    
}