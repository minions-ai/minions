package com.minionslab.core.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionState;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.service.LLMService;
import com.minionslab.core.service.impl.MinionFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class MinionEventPublishingTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    
    private MinionEventPublisher eventPublisher;
    private MinionFactory minionFactory;
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
        
        // Create a test prompt
        testPrompt = MinionPrompt.builder()
            .id(UUID.randomUUID().toString())
            .version("1.0")
            .description("Test prompt")
            .build();
    }

    @Test
    @DisplayName("Should publish state change event when minion state changes")
    void shouldPublishStateChangeEvent() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        
        Minion minion = minionFactory.createMinion(MinionType.USER_DEFINED_AGENT, metadata, testPrompt);
        ArgumentCaptor<MinionStateChangedEvent> eventCaptor = ArgumentCaptor.forClass(MinionStateChangedEvent.class);
        
        // Act
        minion.initialize();
        
        // Assert
        verify(applicationEventPublisher, times(2)).publishEvent(eventCaptor.capture());
        List<MinionStateChangedEvent> events = eventCaptor.getAllValues();
        
        // First transition: CREATED -> INITIALIZING
        MinionStateChangedEvent firstEvent = events.get(0);
        assertNotNull(firstEvent);
        assertEquals(minion.getMinionId(), firstEvent.getMinionId());
        assertEquals(MinionState.CREATED, firstEvent.getOldState());
        assertEquals(MinionState.INITIALIZING, firstEvent.getNewState());
        
        // Second transition: INITIALIZING -> IDLE
        MinionStateChangedEvent secondEvent = events.get(1);
        assertNotNull(secondEvent);
        assertEquals(minion.getMinionId(), secondEvent.getMinionId());
        assertEquals(MinionState.INITIALIZING, secondEvent.getOldState());
        assertEquals(MinionState.IDLE, secondEvent.getNewState());
    }

    @Test
    @DisplayName("Should publish multiple events for multiple state changes")
    void shouldPublishMultipleEvents() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        
        Minion minion = minionFactory.createMinion(MinionType.USER_DEFINED_AGENT, metadata, testPrompt);
        ArgumentCaptor<MinionStateChangedEvent> eventCaptor = ArgumentCaptor.forClass(MinionStateChangedEvent.class);
        
        // Act
        minion.initialize(); // CREATED -> INITIALIZING -> IDLE
        minion.pause();      // IDLE -> WAITING
        minion.resume();     // WAITING -> IDLE
        
        // Assert
        verify(applicationEventPublisher, times(4)).publishEvent(eventCaptor.capture());
        List<MinionStateChangedEvent> events = eventCaptor.getAllValues();
        
        // First transition: CREATED -> INITIALIZING
        assertNotNull(events.get(0));
        assertEquals(MinionState.CREATED, events.get(0).getOldState());
        assertEquals(MinionState.INITIALIZING, events.get(0).getNewState());
        
        // Second transition: INITIALIZING -> IDLE
        assertNotNull(events.get(1));
        assertEquals(MinionState.INITIALIZING, events.get(1).getOldState());
        assertEquals(MinionState.IDLE, events.get(1).getNewState());
        
        // Third transition: IDLE -> WAITING
        assertNotNull(events.get(2));
        assertEquals(MinionState.IDLE, events.get(2).getOldState());
        assertEquals(MinionState.WAITING, events.get(2).getNewState());
        
        // Fourth transition: WAITING -> IDLE
        assertNotNull(events.get(3));
        assertEquals(MinionState.WAITING, events.get(3).getOldState());
        assertEquals(MinionState.IDLE, events.get(3).getNewState());
    }

    @Test
    @DisplayName("Should include metadata in state change events")
    void shouldIncludeMetadataInEvents() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        
        Minion minion = minionFactory.createMinion(MinionType.USER_DEFINED_AGENT, metadata, testPrompt);
        ArgumentCaptor<MinionStateChangedEvent> eventCaptor = ArgumentCaptor.forClass(MinionStateChangedEvent.class);
        
        // Act
        minion.initialize();
        
        // Assert
        verify(applicationEventPublisher, times(2)).publishEvent(eventCaptor.capture());
        List<MinionStateChangedEvent> events = eventCaptor.getAllValues();
        
        // Check that metadata is included in the events
        for (MinionStateChangedEvent event : events) {
            assertNotNull(event);
            assertEquals(minion.getMinionId(), event.getMinionId());
            assertNotNull(event.getMetadata());
        }
    }
} 