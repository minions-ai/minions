package com.minionslab.core.domain;


import com.minionslab.core.domain.enums.MinionState;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a lifecycle event for a minion.
 */
@Data
@Builder
public class MinionLifecycleEvent {
    private final String minionId;
    private final MinionState oldState;
    private final MinionState newState;
    private final Instant timestamp;
    private final Map<String, Object> metadata;
} 