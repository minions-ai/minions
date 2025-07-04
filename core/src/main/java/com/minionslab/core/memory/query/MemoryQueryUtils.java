package com.minionslab.core.memory.query;

import com.minionslab.core.common.message.Message;
import com.minionslab.core.memory.MemoryManager;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.expression.ExprUtil;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MemoryQueryUtils {
    public static List<Message> getLastNUserMessages(MemoryManager memoryManager, int n, String conversationid) {
        MemoryQuery query = MemoryQuery.builder()
                                       .limit(n)
                                       .expression(ExprUtil.getUserMessagesExpression(conversationid))
                                       .build();
        
        return memoryManager.query(query).stream().filter(Message.class::isInstance).map(message -> message).collect(Collectors.toList());
    }
    
    public static List<Message> getLastNAssistantMessages(MemoryManager memoryManager, int n, String conversationid) {
        return memoryManager.query(
                MemoryQuery.builder()
                           .subsystems(MemorySubsystem.SHORT_TERM)
                           .expression(ExprUtil.getAssistantMessagesExpression(conversationid))
                           .limit(n)
                           .build()
                                  ).stream().filter(Message.class::isInstance).map(message -> message).collect(Collectors.toList());
    }
    
    public static List<Message> getEntitiesOfType(MemoryManager memoryManager, String entityType) {
        return memoryManager.query(
                MemoryQuery.builder()
                           .subsystems(MemorySubsystem.ENTITY)
                           .expression(new QueryBuilder().entityType(entityType).build())
                           .build());
    }
    
    public static List<Object> getTopKSimilarVectors(MemoryManager memoryManager, float[] embedding, int k) {
        throw new UnsupportedOperationException("Vector search is now handled by the adapter, not MemoryQuery.");
    }
    
    public static List<Message> getEpisodesByKeyword(MemoryManager memoryManager, String keyword) {
        return memoryManager.query(
                MemoryQuery.builder()
                           .subsystems(MemorySubsystem.EPISODIC)
                           .expression(new QueryBuilder().keyword(keyword).build())
                           .build());
    }
    
    public static List<Message> getMessagesByMetadata(MemoryManager memoryManager, Map<String, Object> metadata) {
        return memoryManager.query(
                MemoryQuery.builder()
                           .subsystems(MemorySubsystem.SHORT_TERM)
                           .expression(new QueryBuilder().keyword(metadata).build())
                           .build()
                                  ).stream().filter(Message.class::isInstance).map(message -> message).collect(Collectors.toList());
    }
    
    public static List<Message> getMessagesAfter(MemoryManager memoryManager, Instant after, int limit) {
        return memoryManager.query(
                MemoryQuery.builder()
                           .subsystems(MemorySubsystem.SHORT_TERM)
                           .expression(new QueryBuilder().after(after).build())
                           .limit(limit)
                           .build()
                                  ).stream().filter(Message.class::isInstance).collect(Collectors.toList());
    }
} 