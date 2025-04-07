package com.minionslab.core.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.enums.MinionState;
import com.minionslab.core.domain.tools.ToolRegistry;
import com.minionslab.core.event.MinionEvent;
import com.minionslab.core.event.MinionStateChangedEvent;
import com.minionslab.core.service.MinionLifecycleListener;
import java.time.Instant;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MinionLifecycleManagerImplTest {

    @Mock
    private ToolRegistry toolRegistry;
    
    @Mock
    private Minion minion;
    
    @Mock
    private MinionLifecycleListener listener;
    
    private MinionLifecycleManagerImpl lifecycleManager;

    @BeforeEach
    void setUp() {
        lifecycleManager = new MinionLifecycleManagerImpl(toolRegistry);
        when(minion.getMinionId()).thenReturn("test-minion-id");
    }

    @Test
    @DisplayName("Should notify registered listeners when state change event occurs")
    void shouldNotifyRegisteredListeners() {
        // Arrange
        lifecycleManager.registerLifecycleListener(minion, listener);
        MinionStateChangedEvent event = new MinionStateChangedEvent(
            minion.getMinionId(),
            MinionState.CREATED,
            MinionState.INITIALIZING,
            Instant.now(),
            Collections.emptyMap()
        );

        // Act
        lifecycleManager.handleStateChanged(event);

        // Assert
        verify(listener).onLifecycleEvent(event);
    }

    @Test
    @DisplayName("Should not notify unregistered listeners")
    void shouldNotNotifyUnregisteredListeners() {
        // Arrange
        lifecycleManager.registerLifecycleListener(minion, listener);
        lifecycleManager.unregisterLifecycleListener(minion, listener);
        
        MinionStateChangedEvent event = new MinionStateChangedEvent(
            minion.getMinionId(),
            MinionState.CREATED,
            MinionState.INITIALIZING,
            Instant.now(),
            Collections.emptyMap()
        );

        // Act
        lifecycleManager.handleStateChanged(event);

        // Assert
        verify(listener, never()).onLifecycleEvent(any(MinionEvent.class));
    }

    @Test
    @DisplayName("Should handle multiple listeners for the same minion")
    void shouldHandleMultipleListeners() {
        // Arrange
        MinionLifecycleListener listener2 = mock(MinionLifecycleListener.class);
        lifecycleManager.registerLifecycleListener(minion, listener);
        lifecycleManager.registerLifecycleListener(minion, listener2);
        
        MinionStateChangedEvent event = new MinionStateChangedEvent(
            minion.getMinionId(),
            MinionState.CREATED,
            MinionState.INITIALIZING,
            Instant.now(),
            Collections.emptyMap()
        );

        // Act
        lifecycleManager.handleStateChanged(event);

        // Assert
        verify(listener).onLifecycleEvent(event);
        verify(listener2).onLifecycleEvent(event);
    }

    @Test
    @DisplayName("Should handle listener exceptions gracefully")
    void shouldHandleListenerExceptions() {
        // Arrange
        lifecycleManager.registerLifecycleListener(minion, listener);
        doThrow(new RuntimeException("Test exception"))
            .when(listener)
            .onLifecycleEvent(any(MinionEvent.class));
        
        MinionStateChangedEvent event = new MinionStateChangedEvent(
            minion.getMinionId(),
            MinionState.CREATED,
            MinionState.INITIALIZING,
            Instant.now(),
            Collections.emptyMap()
        );

        // Act & Assert - should not throw exception
        lifecycleManager.handleStateChanged(event);
    }

    @Test
    @DisplayName("Should initialize minion with tools")
    void shouldInitializeMinionWithTools() {
        // Arrange
        when(minion.getToolboxNames()).thenReturn(Collections.singletonList("test-toolbox"));
        when(toolRegistry.getToolbox("test-toolbox")).thenReturn(new Object());

        // Act
        lifecycleManager.initializeMinion(minion);

        // Assert
        verify(minion).initialize();
        verify(minion).getToolboxes();
    }
} 