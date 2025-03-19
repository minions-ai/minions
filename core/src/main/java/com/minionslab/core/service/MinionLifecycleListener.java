package com.minionslab.core.service;

import com.minionslab.core.domain.MinionLifecycleEvent;

/**
 * Interface for listening to minion lifecycle events.
 */
public interface MinionLifecycleListener {
    /**
     * Called when a minion's state changes.
     *
     * @param event The lifecycle event
     */
    void onStateChange(MinionLifecycleEvent event);
} 