package com.minionslab.core.memory;

import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.model.Prompt;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Policy-based in-memory implementation of ModelMemory.
 * Enforces retrieval/storage policies for system, assistant, step goal, and agent goal messages.
 */
public class AgentModelMemory extends ModelMemory {
    // Map: conversationId -> List of messages
    private final Map<String, List<Message>> memory = new ConcurrentHashMap<>();
    // Snapshots for rollback
    private final Map<String, List<Message>> snapshots = new ConcurrentHashMap<>();

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        memory.computeIfAbsent(conversationId, k -> new ArrayList<>()).addAll(messages);
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return new ArrayList<>(memory.getOrDefault(conversationId, Collections.emptyList()));
    }

    @Override
    public void clear(String conversationId) {
        memory.remove(conversationId);
    }

    @Override
    public String summarize(String conversationId) {
        // Not implemented
        return null;
    }

    @Override
    public void savePrompt(String conversationId, Prompt prompt) {
        saveAll(conversationId, prompt.getMessages());
    }

    @Override
    public void saveChatResponse(String conversationId, ChatResponse response) {
        // Not implemented for this in-memory version
    }

    /**
     * Retrieves messages by scope, enforcing policy:
     * - System messages: Only agent-level system messages are retrieved.
     * - Assistant messages: Always retrieved.
     * - Step goal messages: Not retrieved in subsequent steps.
     * - Agent goal messages: Always retrieved.
     */
    @Override
    public List<Message> findByScope(String conversationId, MessageScope scope) {
        List<Message> all = memory.getOrDefault(conversationId, Collections.emptyList());
        return all.stream()
                .filter(msg -> {
                    if (msg.getScope() == scope) {
                        if (msg.getRole() == MessageRole.SYSTEM) {
                            // Only retrieve agent-level system messages
                            return scope == MessageScope.AGENT;
                        } else if (msg.getRole() == MessageRole.ASSISTANT) {
                            return true;
                        } else if (msg.getScope() == MessageScope.STEP && isStepGoalMessage(msg)) {
                            // Step goal messages are not retrieved in subsequent steps
                            return false;
                        } else if (msg.getScope() == MessageScope.AGENT && isAgentGoalMessage(msg)) {
                            // Agent goal messages are always retrieved
                            return true;
                        }
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves messages by scope and role, enforcing policy.
     */
    @Override
    public List<Message> findByScopeAndRole(String conversationId, MessageScope scope, MessageRole role) {
        return findByScope(conversationId, scope).stream()
                .filter(msg -> msg.getRole() == role)
                .collect(Collectors.toList());
    }

    /**
     * Determines if a message is a step goal message.
     * (This can be customized based on metadata or content.)
     */
    private boolean isStepGoalMessage(Message msg) {
        // Example: check metadata or a special flag
        Object isGoal = msg.getMetadata() != null ? msg.getMetadata().get("stepGoal") : null;
        return Boolean.TRUE.equals(isGoal);
    }

    /**
     * Determines if a message is an agent goal message.
     * (This can be customized based on metadata or content.)
     */
    private boolean isAgentGoalMessage(Message msg) {
        Object isGoal = msg.getMetadata() != null ? msg.getMetadata().get("agentGoal") : null;
        return Boolean.TRUE.equals(isGoal);
    }

    @Override
    public List<Message> getPromptMessages(String conversationId) {
        List<Message> all = memory.getOrDefault(conversationId, Collections.emptyList());
        return all.stream()
                .filter(msg ->
                        // Agent-level system messages
                        (msg.getRole() == MessageRole.SYSTEM && msg.getScope() == MessageScope.AGENT)
                        // Assistant messages (all scopes)
                        || (msg.getRole() == MessageRole.ASSISTANT)
                        // Agent goal messages (always included)
                        || (msg.getScope() == MessageScope.AGENT && isAgentGoalMessage(msg))
                )
                .collect(Collectors.toList());
    }

    @Override
    public void takeSnapshot(String conversationId) {
        List<Message> current = memory.getOrDefault(conversationId, Collections.emptyList());
        // Deep copy if needed, here shallow copy is used
        snapshots.put(conversationId, new ArrayList<>(current));
    }

    @Override
    public void restoreSnapshot(String conversationId) {
        if (snapshots.containsKey(conversationId)) {
            memory.put(conversationId, new ArrayList<>(snapshots.get(conversationId)));
        }
    }
} 