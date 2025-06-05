package com.minionslab.core.memory;

import com.minionslab.core.common.chain.ProcessResult;
import com.minionslab.core.message.Message;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MemoryResult represents the outcome of a memory operation in the MCP framework.
 * It extends {@link ProcessResult} to provide additional result handling for memory messages.
 * <p>
 * This class is designed for extensibility: you can add fields for custom metadata, error tracking,
 * or advanced result aggregation. It is the primary result type for memory strategies and processors.
 *
 * @param <T> the type of Message held in the result
 */
@Data
@Accessors(chain = true)
public class MemoryResult<T extends Message> extends ProcessResult<T> {

    /**
     * Constructs a MemoryResult with all fields.
     *
     * @param processorId the processor ID
     * @param handled whether the operation was handled
     * @param result the list of messages
     * @param error the error thrown, if any
     * @param startedAt the start time
     * @param endedAt the end time
     */
    public MemoryResult(String processorId, boolean handled, List<T> result, Throwable error, Instant startedAt, Instant endedAt) {
        super(processorId, handled, result, error, startedAt, endedAt);
    }
    

    
}
