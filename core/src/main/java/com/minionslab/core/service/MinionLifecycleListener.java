package com.minionslab.core.service;

import com.minionslab.core.event.MinionEvent;

/**
 * Listener interface for minion lifecycle events.
 */
public interface MinionLifecycleListener {
    
    /**
     * Called when a lifecycle event occurs.
     *
     * @param event The lifecycle event
     */
    void onLifecycleEvent(MinionEvent event);
} 