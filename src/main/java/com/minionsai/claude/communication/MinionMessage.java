package com.minionsai.claude.communication;



import com.minionsai.claude.context.ExecutionContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Message class representing communication between Minions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinionMessage {
  private UUID id;
  private UUID conversationId;
  private String senderId;
  private String targetId;
  private String messageType;
  private Object payload;
  private ExecutionContext context;
  private Instant timestamp;

  public static MinionMessage create(String senderId, String targetId, String messageType, Object payload, ExecutionContext context) {
    return MinionMessage.builder()
        .id(UUID.randomUUID())
        .conversationId(UUID.randomUUID())
        .senderId(senderId)
        .targetId(targetId)
        .messageType(messageType)
        .payload(payload)
        .context(context)
        .timestamp(Instant.now())
        .build();
  }

  public static MinionMessage reply(MinionMessage original, Object payload) {
    return MinionMessage.builder()
        .id(UUID.randomUUID())
        .conversationId(original.getConversationId())
        .senderId(original.getTargetId())
        .targetId(original.getSenderId())
        .messageType("REPLY")
        .payload(payload)
        .context(original.getContext())
        .timestamp(Instant.now())
        .build();
  }
}