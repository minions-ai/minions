package com.minionsai.claude.communication;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Interface for a messaging system between Minions
 */
public interface MinionMessenger {
  void sendMessage(MinionMessage message);
  CompletableFuture<MinionMessage> sendAndReceive(MinionMessage message);
  void registerMessageHandler(String minionId, Consumer<MinionMessage> handler);
  void unregisterMessageHandler(String minionId);
}
