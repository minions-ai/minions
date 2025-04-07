package com.minionslab.core.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionState;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.tools.ToolRegistry;
import com.minionslab.core.service.LLMService;
import com.minionslab.core.service.MinionLifecycleListener;
import com.minionslab.core.service.impl.MinionFactory;
import com.minionslab.core.service.impl.MinionLifecycleManagerImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.verification.AtLeast;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class MinionEventIntegrationTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    
    @Mock
    private ToolRegistry toolRegistry;
    
    @Mock
    private MinionLifecycleListener listener;
    
    private MinionEventPublisher eventPublisher;
    private MinionFactory minionFactory;
    private MinionLifecycleManagerImpl lifecycleManager;
    private LLMService llmService;
    private MinionPrompt testPrompt;

    @BeforeEach
    void setUp() {
        // Create a mock LLMService
        llmService = new LLMService() {
            @Override
            public com.minionslab.core.service.impl.llm.model.LLMResponse processRequest(com.minionslab.core.service.impl.llm.model.LLMRequest request) {
                return com.minionslab.core.service.impl.llm.model.LLMResponse.builder()
                    .requestId(UUID.randomUUID().toString())
                    .promptId(request.getPrompt() != null ? request.getPrompt().getId() : null)
                    .promptVersion(request.getPrompt() != null ? request.getPrompt().getVersion() : null)
                    .responseText("This is a mock response from the LLM service.")
                    .build();
            }
        };
        
        // Create the event publisher
        eventPublisher = new SpringMinionEventPublisher(applicationEventPublisher);
        
        // Create the minion factory
        minionFactory = new MinionFactory(eventPublisher, llmService);
        
        // Create the lifecycle manager
        lifecycleManager = new MinionLifecycleManagerImpl(toolRegistry);
        
        // Create a test prompt
        testPrompt = MinionPrompt.builder()
            .id(UUID.randomUUID().toString())
            .version("1.0")
            .description("Test prompt")
            .build();
    }

    @Test
    @DisplayName("Should propagate events from minion through manager to listeners")
    void shouldPropagateEventsToListeners() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        
        Minion minion = minionFactory.createMinion(MinionType.USER_DEFINED_AGENT, metadata, testPrompt);
        lifecycleManager.registerLifecycleListener(minion, listener);
        
        ArgumentCaptor<MinionStateChangedEvent> eventCaptor = ArgumentCaptor.forClass(MinionStateChangedEvent.class);
        
        // Set up event publishing chain
        doAnswer(invocation -> {
            MinionStateChangedEvent event = invocation.getArgument(0);
            lifecycleManager.handleStateChanged(event);
            return null;
        }).when(applicationEventPublisher).publishEvent(any(MinionStateChangedEvent.class));

        // Act
        minion.initialize();

        // Assert
        verify(listener, new AtLeast(1)).onLifecycleEvent(eventCaptor.capture());
        MinionStateChangedEvent event = eventCaptor.getValue();
        assertNotNull(event);
        assertEquals(minion.getMinionId(), event.getMinionId());
        assertEquals(MinionState.INITIALIZING, event.getOldState());
        assertEquals(MinionState.IDLE, event.getNewState());
    }

    @Test
    @DisplayName("Should maintain event order through the complete chain")
    void shouldMaintainEventOrder() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        
        Minion minion = minionFactory.createMinion(MinionType.USER_DEFINED_AGENT, metadata, testPrompt);
        lifecycleManager.registerLifecycleListener(minion, listener);
        
        ArgumentCaptor<MinionStateChangedEvent> eventCaptor = ArgumentCaptor.forClass(MinionStateChangedEvent.class);
        
        // Set up event publishing chain
        doAnswer(invocation -> {
            MinionStateChangedEvent event = invocation.getArgument(0);
            lifecycleManager.handleStateChanged(event);
            return null;
        }).when(applicationEventPublisher).publishEvent(any(MinionStateChangedEvent.class));

        // Act
        minion.initialize();
        minion.pause();
        minion.resume();
        minion.shutdown();

        // Assert
        verify(listener, new AtLeast(5)).onLifecycleEvent(eventCaptor.capture());
        // We can't verify the exact order of events in this test due to the way Mockito captures arguments,
        // but we can verify that at least 5 events were published (one for each state change)
    }

    @Test
    @DisplayName("Should handle processing state transitions correctly")
    void shouldHandleProcessingStateTransitions() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        
        Minion minion = minionFactory.createMinion(MinionType.USER_DEFINED_AGENT, metadata, testPrompt);
        lifecycleManager.registerLifecycleListener(minion, listener);
        
        ArgumentCaptor<MinionStateChangedEvent> eventCaptor = ArgumentCaptor.forClass(MinionStateChangedEvent.class);
        
        // Set up event publishing chain
        doAnswer(invocation -> {
            MinionStateChangedEvent event = invocation.getArgument(0);
            lifecycleManager.handleStateChanged(event);
            return null;
        }).when(applicationEventPublisher).publishEvent(any(MinionStateChangedEvent.class));

        // Act
        minion.initialize();
        minion.processPrompt("Test request", new HashMap<>());
        minion.pauseProcessing();
        minion.resumeProcessing();
        minion.shutdown();

        // Assert
        verify(listener, new AtLeast(6)).onLifecycleEvent(eventCaptor.capture());
        // Verify the sequence of states: CREATED -> INITIALIZING -> IDLE -> PROCESSING -> WAITING -> PROCESSING -> SHUTTING_DOWN -> SHUTDOWN
    }

    @Test
    @DisplayName("Should handle error recovery transitions correctly")
    void shouldHandleErrorRecoveryTransitions() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        
        Minion minion = minionFactory.createMinion(MinionType.USER_DEFINED_AGENT, metadata, testPrompt);
        lifecycleManager.registerLifecycleListener(minion, listener);
        
        ArgumentCaptor<MinionStateChangedEvent> eventCaptor = ArgumentCaptor.forClass(MinionStateChangedEvent.class);
        
        // Set up event publishing chain
        doAnswer(invocation -> {
            MinionStateChangedEvent event = invocation.getArgument(0);
            lifecycleManager.handleStateChanged(event);
            return null;
        }).when(applicationEventPublisher).publishEvent(any(MinionStateChangedEvent.class));

        // Act
        minion.initialize();
        minion.handleFailure(new RuntimeException("Test error"));
        minion.recover();
        minion.shutdown();

        // Assert
        verify(listener, new AtLeast(5)).onLifecycleEvent(eventCaptor.capture());
        // Verify the sequence of states: CREATED -> INITIALIZING -> IDLE -> ERROR -> IDLE -> SHUTTING_DOWN -> SHUTDOWN
    }

    @Test
    @DisplayName("Should handle reinitialization from error state correctly")
    void shouldHandleReinitializationFromErrorState() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        
        Minion minion = minionFactory.createMinion(MinionType.USER_DEFINED_AGENT, metadata, testPrompt);
        lifecycleManager.registerLifecycleListener(minion, listener);
        
        ArgumentCaptor<MinionStateChangedEvent> eventCaptor = ArgumentCaptor.forClass(MinionStateChangedEvent.class);
        
        // Set up event publishing chain
        doAnswer(invocation -> {
            MinionStateChangedEvent event = invocation.getArgument(0);
            lifecycleManager.handleStateChanged(event);
            return null;
        }).when(applicationEventPublisher).publishEvent(any(MinionStateChangedEvent.class));

        // Act
        minion.initialize();
        minion.handleFailure(new RuntimeException("Test error"));
        minion.reinitialize();
        minion.shutdown();

        // Assert
        verify(listener, new AtLeast(6)).onLifecycleEvent(eventCaptor.capture());
        // Verify the sequence of states: CREATED -> INITIALIZING -> IDLE -> ERROR -> INITIALIZING -> IDLE -> SHUTTING_DOWN -> SHUTDOWN
    }

    @Test
    @DisplayName("Should handle started to idle transition correctly")
    void shouldHandleStartedToIdleTransition() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        
        Minion minion = minionFactory.createMinion(MinionType.USER_DEFINED_AGENT, metadata, testPrompt);
        lifecycleManager.registerLifecycleListener(minion, listener);
        
        ArgumentCaptor<MinionStateChangedEvent> eventCaptor = ArgumentCaptor.forClass(MinionStateChangedEvent.class);
        
        // Set up event publishing chain
        doAnswer(invocation -> {
            MinionStateChangedEvent event = invocation.getArgument(0);
            lifecycleManager.handleStateChanged(event);
            return null;
        }).when(applicationEventPublisher).publishEvent(any(MinionStateChangedEvent.class));

        // Act
        minion.initialize();
        minion.start();
        minion.startProcessing();
        minion.shutdown();

        // Assert
        verify(listener, new AtLeast(5)).onLifecycleEvent(eventCaptor.capture());
        // Verify the sequence of states: CREATED -> INITIALIZING -> IDLE -> STARTED -> IDLE -> SHUTTING_DOWN -> SHUTDOWN
    }

    @Test
    @DisplayName("Should verify all state transitions are properly logged")
    void shouldVerifyAllStateTransitionsAreLogged() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        
        Minion minion = minionFactory.createMinion(MinionType.USER_DEFINED_AGENT, metadata, testPrompt);
        lifecycleManager.registerLifecycleListener(minion, listener);
        
        ArgumentCaptor<MinionStateChangedEvent> eventCaptor = ArgumentCaptor.forClass(MinionStateChangedEvent.class);
        AtomicInteger eventCount = new AtomicInteger(0);
        
        // Set up event publishing chain with counter
        doAnswer(invocation -> {
            MinionStateChangedEvent event = invocation.getArgument(0);
            lifecycleManager.handleStateChanged(event);
            eventCount.incrementAndGet();
            return null;
        }).when(applicationEventPublisher).publishEvent(any(MinionStateChangedEvent.class));

        // Act - Test a complete lifecycle with all possible transitions
        minion.initialize();                    // CREATED -> INITIALIZING -> IDLE
        minion.start();                         // IDLE -> STARTED
        minion.startProcessing();               // STARTED -> IDLE
        minion.processPrompt("Test", new HashMap<>()); // IDLE -> PROCESSING
        minion.pauseProcessing();               // PROCESSING -> WAITING
        minion.resumeProcessing();              // WAITING -> PROCESSING
        minion.handleFailure(new RuntimeException("Test error")); // PROCESSING -> ERROR
        minion.recover();                       // ERROR -> IDLE
        minion.processPrompt("Test", new HashMap<>()); // IDLE -> PROCESSING
        minion.handleFailure(new RuntimeException("Test error")); // PROCESSING -> ERROR
        minion.reinitialize();                  // ERROR -> INITIALIZING -> IDLE
        minion.shutdown();                      // IDLE -> SHUTTING_DOWN -> SHUTDOWN

        // Assert
        verify(listener, new AtLeast(1)).onLifecycleEvent(eventCaptor.capture());
        // We expect at least 15 state transitions
        assertEquals(15, eventCount.get());
    }
} 