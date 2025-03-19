package com.minionslab.core.domain;


import com.minionslab.core.domain.enums.MinionState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

/**
 * Tracks metrics for a minion.
 */
@Data
public class MinionMetrics {
    private final String minionId;
    private final Map<MinionState, Integer> stateTransitions = new ConcurrentHashMap<>();
    private final Map<String, Long> timings = new ConcurrentHashMap<>();
    private final List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

    /**
     * Record a state transition
     */
    public void recordStateTransition(MinionState from, MinionState to) {
        stateTransitions.merge(to, 1, Integer::sum);
        timings.put("last_" + to.name().toLowerCase(), System.currentTimeMillis());
    }

    /**
     * Record an error
     */
    public void recordError(Exception error) {
        errors.add(error);
    }

    /**
     * Get the number of times a state has been entered
     */
    public int getStateCount(MinionState state) {
        return stateTransitions.getOrDefault(state, 0);
    }

    /**
     * Get the last time a state was entered
     */
    public Long getLastStateTime(MinionState state) {
        return timings.get("last_" + state.name().toLowerCase());
    }

    /**
     * Get the number of errors recorded
     */
    public int getErrorCount() {
        return errors.size();
    }
} 