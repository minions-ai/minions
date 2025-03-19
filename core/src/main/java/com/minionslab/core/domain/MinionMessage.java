package com.minionslab.core.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Represents a message between minions.
 * Supports different message types and payloads.
 */
@Data
@Builder
@Accessors(chain = true)
public class MinionMessage {
    /**
     * Unique identifier for the message
     */
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    /**
     * Timestamp when the message was created
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * ID of the sender minion
     */
    private String senderId;

    /**
     * ID of the recipient minion
     */
    private String recipientId;

    /**
     * Type of the message
     */
    private MessageType type;

    /**
     * Priority of the message (higher number = higher priority)
     */
    @Builder.Default
    private int priority = 0;

    /**
     * The actual message payload
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private Object payload;

    /**
     * Additional metadata for the message
     */
    @Builder.Default
    private Map<String, Object> metadata = Map.of();

    /**
     * Whether this message requires acknowledgment
     */
    @Builder.Default
    private boolean requiresAck = false;

    /**
     * Time-to-live in milliseconds (0 for infinite)
     */
    @Builder.Default
    private long ttl = 0;

    /**
     * Types of messages supported by the system
     */
    public enum MessageType {
        REQUEST,        // Request for action
        RESPONSE,       // Response to a request
        NOTIFICATION,   // Informational message
        ERROR,         // Error message
        HEARTBEAT,     // Keep-alive message
        CONTROL        // System control message
    }
} 