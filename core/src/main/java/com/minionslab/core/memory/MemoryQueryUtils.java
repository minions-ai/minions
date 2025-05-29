package com.minionslab.core.memory;

import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

public class MemoryQueryUtils {
    public static List<Message> getLastNUserMessages(MemoryManager memoryManager, int n) {
        return memoryManager.query(
            MemoryQuery.builder()
                .subsystems(Set.of(MemoryQuery.MemorySubsystem.SHORT_TERM))
                .role(MessageRole.USER)
                .limit(n)
                .build()
        ).stream().filter(Message.class::isInstance).map(Message.class::cast).collect(Collectors.toList());
    }

    public static List<Message> getLastNAssistantMessages(MemoryManager memoryManager, int n) {
        return memoryManager.query(
            MemoryQuery.builder()
                .subsystems(Set.of(MemoryQuery.MemorySubsystem.SHORT_TERM))
                .role(MessageRole.ASSISTANT)
                .limit(n)
                .build()
        ).stream().filter(Message.class::isInstance).map(Message.class::cast).collect(Collectors.toList());
    }

    public static List<Message> getEntitiesOfType(MemoryManager memoryManager, String entityType) {
        return memoryManager.query(
            MemoryQuery.builder()
                .subsystems(Set.of(MemoryQuery.MemorySubsystem.ENTITY))
                .entityType(entityType)
                .build()
        );
    }

    public static List<Object> getTopKSimilarVectors(MemoryManager memoryManager, float[] embedding, int k) {
        throw new UnsupportedOperationException("Vector search is now handled by the adapter, not MemoryQuery.");
    }

    public static List<Message> getEpisodesByKeyword(MemoryManager memoryManager, String keyword) {
        return memoryManager.query(
            MemoryQuery.builder()
                .subsystems(Set.of(MemoryQuery.MemorySubsystem.EPISODIC))
                .keyword(keyword)
                .build()
        );
    }

    public static List<Message> getMessagesByMetadata(MemoryManager memoryManager, Map<String, Object> metadata) {
        return memoryManager.query(
            MemoryQuery.builder()
                .subsystems(Set.of(MemoryQuery.MemorySubsystem.SHORT_TERM))
                .metadata(metadata)
                .build()
        ).stream().filter(Message.class::isInstance).map(Message.class::cast).collect(Collectors.toList());
    }

    public static List<Message> getMessagesAfter(MemoryManager memoryManager, Instant after, int limit) {
        return memoryManager.query(
            MemoryQuery.builder()
                .subsystems(Set.of(MemoryQuery.MemorySubsystem.SHORT_TERM))
                .after(after)
                .limit(limit)
                .build()
        ).stream().filter(Message.class::isInstance).map(Message.class::cast).collect(Collectors.toList());
    }
} 