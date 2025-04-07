package com.minionslab.core.event;

/**
 * Interface for publishing minion-related events.
 */
public interface MinionEventPublisher {
    void publishEvent(MinionEvent event);
} 