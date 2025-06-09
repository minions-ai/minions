package com.minionslab.core.model;

import com.minionslab.core.common.message.Message;
import com.minionslab.core.common.message.MessageRole;

import java.util.*;

/**
 * Generic message bundle that can hold multiple different components of a message set, grouped by role.
 * Supports multiple messages per role (e.g., conversation history).
 *
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Extend MessageBundle to add custom grouping, filtering, or message management logic.</li>
 *   <li>Override methods to support advanced message retrieval, ordering, or metadata.</li>
 * </ul>
 * <b>Usage:</b> MessageBundle groups messages by role and supports conversation history management. Extend for advanced message orchestration.
 */
public class MessageBundle {
    private final Map<MessageRole, List<Message>> messagesByRole = new LinkedHashMap<>();
    
    public MessageBundle() {
    }
    
    public MessageBundle(MessageBundle messageBundle) {
        if (messageBundle != null && messageBundle.messagesByRole != null)
            messagesByRole.putAll(messageBundle.messagesByRole);
//       messageBundle.messagesByRole.forEach((role, msgs) -> messagesByRole.put(role, new ArrayList<>(msgs)));
    
    }
    
    public MessageBundle(List<Message> messages) {
        messages.forEach(this::addMessage);
    }
    
    /**
     * Adds a message to the bundle, grouped by its role.
     *
     * @param message The message to add
     */
    public void addMessage(Message message) {
        messagesByRole.computeIfAbsent(message.getRole(), k -> new ArrayList<>()).add(message);
    }
    
    /**
     * Adds a list of messages to the bundle for a given role.
     *
     * @param role     The message role
     * @param messages The messages to add
     */
    public void addMessages(MessageRole role, List<Message> messages) {
        messagesByRole.computeIfAbsent(role, k -> new ArrayList<>()).addAll(messages);
    }
    
    /**
     * Gets the list of messages for a role, or an empty list if none.
     *
     * @param role The message role
     * @return List of messages
     */
    public List<Message> getMessages(MessageRole role) {
        return messagesByRole.getOrDefault(role, Collections.emptyList());
    }
    
    /**
     * Returns all messages in all roles, in insertion order.
     *
     * @return Flattened list of all messages
     */
    public List<Message> getAllMessages() {
        List<Message> all = new ArrayList<>();
        for (List<Message> msgs : messagesByRole.values()) {
            all.addAll(msgs);
        }
        return all;
    }
    
    /**
     * Returns the internal map of messages by role (unmodifiable).
     */
    public Map<MessageRole, List<Message>> getMessagesByRole() {
        return Collections.unmodifiableMap(messagesByRole);
    }
} 