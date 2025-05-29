package com.minionslab.core.memory;

import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Query object for retrieving messages from memory, supporting various filters and options.
 */
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class MemoryQuery {
    public enum MemorySubsystem { SHORT_TERM, ENTITY, VECTOR, EPISODIC }

    private final String messageId;
    private final Set<MemorySubsystem> subsystems;
    private final MessageRole role;
    private final MessageScope scope;
    private final String keyword;
    private final float[] embedding;
    private final int topK;
    private final Instant after;
    private final Instant before;
    private final Map<String, Object> metadata;
    private final String entityType;
    private final int limit;
}