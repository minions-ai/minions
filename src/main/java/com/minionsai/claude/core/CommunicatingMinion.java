package com.minionsai.claude.core;

import com.minionsai.claude.communication.MinionMessage;
import com.minionsai.claude.communication.MinionMessenger;
import com.minionsai.claude.context.ExecutionContext;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced Minion base class with communication capabilities
 */
public abstract class CommunicatingMinion extends Minion {

  private final MinionMessenger messenger;

  public CommunicatingMinion(String id, String specialization, MinionMessenger messenger, String name, SystemPrompt systemPrompt) {
    super(id, name, specialization, systemPrompt);
    this.messenger = messenger;

    // Register message handler
    messenger.registerMessageHandler(id, this::onMessageReceived);
  }

  protected void onMessageReceived(MinionMessage message) {
    // Default implementation - override in subclasses
  }

  protected void sendMessage(String targetId, String messageType, Object payload, ExecutionContext context) {
    MinionMessage message = MinionMessage.create(getId(), targetId, messageType, payload, context);
    messenger.sendMessage(message);
  }

  protected CompletableFuture<MinionMessage> sendAndReceive(String targetId, String messageType, Object payload, ExecutionContext context) {
    MinionMessage message = MinionMessage.create(getId(), targetId, messageType, payload, context);
    return messenger.sendAndReceive(message);
  }

  @Override public void finalizeMinion() {
    messenger.unregisterMessageHandler(getId());
  }
}

