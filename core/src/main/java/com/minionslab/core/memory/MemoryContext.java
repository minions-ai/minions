package com.minionslab.core.memory;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.message.Message;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MemoryContext encapsulates the state and request for a memory operation in the MCP framework.
 * It acts as the context object passed through memory strategies and processors, carrying
 * the operation type, request details, and accumulated results.
 * <p>
 * This class is designed for extensibility: you can add fields for custom metadata, tracking,
 * or advanced memory operations. It is the primary carrier for information as memory strategies
 * are chained and composed.
 */
@Data
@Accessors(chain = true)
public class MemoryContext implements ProcessContext<MemoryResult<Message>> {
    /**
     * The memory request, including query and messages to store.
     */
    private MemoryRequest memoryRequest;
    /**
     * The type of memory operation (store, retrieve, query, etc.).
     */
    private MemoryOperation operation;
    /**
     * The list of results accumulated during processing.
     */
    private List<MemoryResult<Message>> results = new ArrayList<>();

    /**
     * Add a result to the context.
     *
     * @param result the result to add
     */
    @Override
    public void addResult(MemoryResult<Message> result) {
        results.add(result);
    }
}
