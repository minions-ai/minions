package com.minionslab.core.event;

import com.minionslab.core.domain.enums.MinionState;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import lombok.Value;

/**
 * Event that represents a state change in a minion.
 */
@Value
public class MinionStateChangedEvent implements MinionEvent {
    String minionId;
    MinionState oldState;
    MinionState newState;
    Instant timestamp;
    Map<String, Object> metadata;
    
    public static MinionStateChangedEvent of(String minionId, MinionState oldState, MinionState newState) {
        return new MinionStateChangedEvent(
            minionId,
            oldState,
            newState,
            Instant.now(),
            Collections.emptyMap()
        );
    }
} 