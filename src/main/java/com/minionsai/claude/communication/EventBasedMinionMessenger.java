package com.minionsai.claude.communication;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Spring Event-based implementation of MinionMessenger
 */
@Component
@AllArgsConstructor
public class EventBasedMinionMessenger implements MinionMessenger {
  private final ApplicationEventPublisher eventPublisher;
  private final ConcurrentHashMap<String, Consumer<MinionMessage>> messageHandlers = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<UUID, CompletableFuture<MinionMessage>> pendingResponses = new ConcurrentHashMap<>();

  @Override
  public void sendMessage(MinionMessage message) {
    eventPublisher.publishEvent(new MinionMessageEvent(message));
  }

  @Override
  public CompletableFuture<MinionMessage> sendAndReceive(MinionMessage message) {
    CompletableFuture<MinionMessage> future = new CompletableFuture<>();
    pendingResponses.put(message.getConversationId(), future);
    sendMessage(message);
    return future;
  }

  @Override
  public void registerMessageHandler(String minionId, Consumer<MinionMessage> handler) {
    messageHandlers.put(minionId, handler);
  }

  @Override
  public void unregisterMessageHandler(String minionId) {
    messageHandlers.remove(minionId);
  }

  @EventListener
  public void handleMinionMessage(MinionMessageEvent event) {
    MinionMessage message = event.getMessage();

    // Handle replies to pending requests
    if ("REPLY".equals(message.getMessageType())) {
      CompletableFuture<MinionMessage> future = pendingResponses.remove(message.getConversationId());
      if (future != null) {
        future.complete(message);
        return;
      }
    }

    // Deliver to target minion if registered
    Consumer<MinionMessage> handler = messageHandlers.get(message.getTargetId());
    if (handler != null) {
      handler.accept(message);
    }
  }
}