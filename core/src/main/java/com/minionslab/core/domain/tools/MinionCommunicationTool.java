package com.minionslab.core.domain.tools;

import com.minionslab.core.common.annotation.Tool;
import com.minionslab.core.domain.MinionMessage;
import com.minionslab.core.service.MinionMessageHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Tool for handling asynchronous communication between minions.
 * Uses Spring's ApplicationContext for message routing.
 */
@Slf4j
@Component
public class MinionCommunicationTool {

    private final ApplicationContext applicationContext;
    private final Map<String, MinionMessageHandler> messageHandlers;
    private final ExecutorService executorService;

    @Autowired
    public MinionCommunicationTool(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.messageHandlers = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Registers a message handler for a minion
     *
     * @param handler The message handler to register
     */
    public void registerHandler(MinionMessageHandler handler) {
        String minionId = handler.getMinionId();
        messageHandlers.put(minionId, handler);
        log.info("Registered message handler for minion: {}", minionId);
    }

    /**
     * Unregisters a message handler for a minion
     *
     * @param minionId The ID of the minion to unregister
     */
    public void unregisterHandler(String minionId) {
        messageHandlers.remove(minionId);
        log.info("Unregistered message handler for minion: {}", minionId);
    }

    /**
     * Sends a message to a specific minion
     *
     * @param message The message to send
     * @return true if the message was sent successfully, false otherwise
     */
    @Tool(
        id = "sendMessage",
        name = "Send Message",
        description = "Send a message to another minion",
        categories = {"communication"}
    )
    public boolean sendMessage(MinionMessage message) {
        if (message.getRecipientId() == null || message.getRecipientId().isEmpty()) {
            log.error("Cannot send message: recipient ID is required");
            return false;
        }

        MinionMessageHandler handler = messageHandlers.get(message.getRecipientId());
        if (handler == null) {
            log.error("No message handler found for recipient: {}", message.getRecipientId());
            return false;
        }

        // Process message asynchronously
        executorService.submit(() -> {
            try {
                boolean handled = handler.handleMessage(message);
                if (handled) {
                    log.debug("Message {} handled successfully by minion {}", 
                        message.getId(), message.getRecipientId());
                } else {
                    log.warn("Message {} was not handled by minion {}", 
                        message.getId(), message.getRecipientId());
                }
            } catch (Exception e) {
                log.error("Error handling message {} by minion {}", 
                    message.getId(), message.getRecipientId(), e);
            }
        });

        return true;
    }

    /**
     * Broadcasts a message to all registered minions
     *
     * @param message The message to broadcast
     * @return The number of minions that received the message
     */
    @Tool(
        id = "broadcastMessage",
        name = "Broadcast Message",
        description = "Send a message to all registered minions",
        categories = {"communication"}
    )
    public int broadcastMessage(MinionMessage message) {
        if (message.getRecipientId() != null && !message.getRecipientId().isEmpty()) {
            log.warn("Broadcast message should not have a recipient ID");
            return 0;
        }

        int sentCount = 0;
        for (Map.Entry<String, MinionMessageHandler> entry : messageHandlers.entrySet()) {
            // Skip the sender
            if (entry.getKey().equals(message.getSenderId())) {
                continue;
            }

            MinionMessage broadcastMessage = MinionMessage.builder()
                .senderId(message.getSenderId())
                .recipientId(entry.getKey())
                .type(message.getType())
                .payload(message.getPayload())
                .metadata(message.getMetadata())
                .requiresAck(message.isRequiresAck())
                .ttl(message.getTtl())
                .build();

            if (sendMessage(broadcastMessage)) {
                sentCount++;
            }
        }

        log.info("Broadcast message {} sent to {} minions", message.getId(), sentCount);
        return sentCount;
    }

    /**
     * Sends a response to a specific message
     *
     * @param originalMessage The original message to respond to
     * @param responsePayload The response payload
     * @return true if the response was sent successfully, false otherwise
     */
    @Tool(
        id = "sendResponse",
        name = "Send Response",
        description = "Send a response to a specific message",
        categories = {"communication"}
    )
    public boolean sendResponse(MinionMessage originalMessage, Object responsePayload) {
        MinionMessage response = MinionMessage.builder()
            .senderId(originalMessage.getRecipientId())
            .recipientId(originalMessage.getSenderId())
            .type(MinionMessage.MessageType.RESPONSE)
            .payload(responsePayload)
            .metadata(Map.of("originalMessageId", originalMessage.getId()))
            .build();

        return sendMessage(response);
    }

    /**
     * Shuts down the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }
} 