package com.minionslab.core.service.impl;

import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.enums.MinionState;
import com.minionslab.core.domain.tools.ToolRegistry;
import com.minionslab.core.event.MinionEvent;
import com.minionslab.core.event.MinionStateChangedEvent;
import com.minionslab.core.service.MinionLifecycleListener;
import com.minionslab.core.service.MinionLifecycleManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinionLifecycleManagerImpl implements MinionLifecycleManager {

    private final ToolRegistry toolRegistry;
    private final Map<String, List<MinionLifecycleListener>> lifecycleListeners = new ConcurrentHashMap<>();
    private final Map<String, MinionState> previousStates = new ConcurrentHashMap<>();

    @EventListener
    public void handleStateChanged(MinionStateChangedEvent event) {
        List<MinionLifecycleListener> listeners = lifecycleListeners.get(event.getMinionId());
        if (listeners != null) {
            for (MinionLifecycleListener listener : listeners) {
                try {
                    listener.onLifecycleEvent(event);
                } catch (Exception e) {
                    log.error("Error notifying listener for minion: {}", event.getMinionId(), e);
                }
            }
        }
    }

    @Override
    public void initializeMinion(Minion minion) {
        log.debug("Initializing minion: {}", minion.getMinionId());

        try {
            // Set up tools
            if (minion.getToolboxNames() != null && !minion.getToolboxNames().isEmpty()) {
                registerToolsForMinion(minion);
            }

            // Initialize the minion
            minion.initialize();

            log.info("Initialized minion: {}", minion.getMinionId());
        } catch (Exception e) {
            log.error("Failed to initialize minion: {}", minion.getMinionId(), e);
            throw new MinionException("Failed to initialize minion: " + e.getMessage(), e);
        }
    }



    @Override
    public void pauseMinion(Minion minion) {
        log.debug("Pausing minion: {}", minion.getMinionId());

        try {
            minion.pause();
            notifyListeners(minion, "paused");
            log.info("Paused minion: {}", minion.getMinionId());
        } catch (Exception e) {
            log.error("Failed to pause minion: {}", minion.getMinionId(), e);
            throw new MinionException("Failed to pause minion: " + e.getMessage(), e);
        }
    }

    @Override
    public void resumeMinion(Minion minion) {
        log.debug("Resuming minion: {}", minion.getMinionId());

        try {
            minion.resume();
            notifyListeners(minion, "resumed");
            log.info("Resumed minion: {}", minion.getMinionId());
        } catch (Exception e) {
            log.error("Failed to resume minion: {}", minion.getMinionId(), e);
            throw new MinionException("Failed to resume minion: " + e.getMessage(), e);
        }
    }

    @Override
    public void stopMinion(Minion minion) {
        log.debug("Stopping minion: {}", minion.getMinionId());

        try {
            // Clean up tools
            minion.getToolboxNames().clear();
            minion.getToolboxes().clear();

            // Notify listeners before stopping
            notifyListeners(minion, "stopping");

            // Stop the minion
            minion.shutdown();

            // Remove listeners and state tracking
            lifecycleListeners.remove(minion.getMinionId());
            previousStates.remove(minion.getMinionId());

            log.info("Stopped minion: {}", minion.getMinionId());
        } catch (Exception e) {
            log.error("Failed to stop minion: {}", minion.getMinionId(), e);
            throw new MinionException("Failed to stop minion: " + e.getMessage(), e);
        }
    }

    @Override
    public void registerLifecycleListener(Minion minion, MinionLifecycleListener listener) {
        if (minion == null || listener == null) {
            throw new IllegalArgumentException("Minion and listener cannot be null");
        }

        lifecycleListeners.computeIfAbsent(minion.getMinionId(), k -> new ArrayList<>())
            .add(listener);

        log.debug("Registered lifecycle listener for minion: {}", minion.getMinionId());
    }

    @Override
    public void unregisterLifecycleListener(Minion minion, MinionLifecycleListener listener) {
        if (minion == null || listener == null) {
            throw new IllegalArgumentException("Minion and listener cannot be null");
        }

        List<MinionLifecycleListener> listeners = lifecycleListeners.get(minion.getMinionId());
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                lifecycleListeners.remove(minion.getMinionId());
            }
        }

        log.debug("Unregistered lifecycle listener for minion: {}", minion.getMinionId());
    }

    private void registerToolsForMinion(Minion minion) {
        for (String toolboxName : minion.getToolboxNames()) {
            if (!StringUtils.hasText(toolboxName)) {
                log.warn("Empty toolbox name found, skipping");
                continue;
            }

            Object toolbox = toolRegistry.getToolbox(toolboxName);
            if (toolbox != null) {
                minion.getToolboxes().put(toolboxName, toolbox);
                log.debug("Registered toolbox: {} for minion: {}", toolboxName, minion.getMinionId());
            } else {
                log.warn("Toolbox not found: {} for minion: {}", toolboxName, minion.getMinionId());
            }
        }
    }

    private void notifyListeners(Minion minion, String event) {
        List<MinionLifecycleListener> listeners = lifecycleListeners.get(minion.getMinionId());
        MinionState currentState = minion.getState();
        MinionState previousState = previousStates.getOrDefault(minion.getMinionId(), currentState);
        
        MinionStateChangedEvent minionEvent = MinionStateChangedEvent.of(
            minion.getMinionId(),
            previousState,
            currentState
        );
        
        // Update the previous state for next time
        previousStates.put(minion.getMinionId(), currentState);
        
        if (listeners != null) {
            for (MinionLifecycleListener listener : listeners) {
                try {
                    listener.onLifecycleEvent(minionEvent);
                } catch (Exception e) {
                    log.error("Error notifying listener for minion: {}", minion.getMinionId(), e);
                }
            }
        }
    }
}