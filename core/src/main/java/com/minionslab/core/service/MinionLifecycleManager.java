package com.minionslab.core.service;

import com.minionslab.core.domain.Minion;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Service responsible for managing the lifecycle of minions.
 * This includes initialization, starting, pausing, resuming, and cleanup.
 */
public interface MinionLifecycleManager {
    
    /**
     * Initializes a minion with its required components and configuration.
     *
     * @param minion The minion to initialize
     */
    void initializeMinion(Minion minion);
    

    
    /**
     * Pauses a minion, temporarily stopping it from processing requests.
     *
     * @param minion The minion to pause
     */
    void pauseMinion(Minion minion);
    
    /**
     * Resumes a paused minion, allowing it to process requests again.
     *
     * @param minion The minion to resume
     */
    void resumeMinion(Minion minion);
    
    /**
     * Stops a minion and performs cleanup.
     *
     * @param minion The minion to stop
     */
    void stopMinion(Minion minion);
    
    /**
     * Registers a lifecycle listener for a minion.
     *
     * @param minion The minion to register the listener for
     * @param listener The listener to register
     */
    void registerLifecycleListener(Minion minion, MinionLifecycleListener listener);
    
    /**
     * Unregisters a lifecycle listener from a minion.
     *
     * @param minion The minion to unregister the listener from
     * @param listener The listener to unregister
     */
    void unregisterLifecycleListener(Minion minion, MinionLifecycleListener listener);
}
