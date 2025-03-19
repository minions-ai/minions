package com.minionslab.core.service;

import com.minionslab.core.domain.MinionMessage;

/**
 * Interface for handling messages between minions.
 * Minions that want to receive messages should implement this interface.
 */
public interface MinionMessageHandler {
    /**
     * Called when a message is received.
     *
     * @param message The received message
     * @return true if the message was handled successfully, false otherwise
     */
    boolean handleMessage(MinionMessage message);

    /**
     * Gets the ID of the minion implementing this handler.
     *
     * @return The minion's ID
     */
    String getMinionId();
} 