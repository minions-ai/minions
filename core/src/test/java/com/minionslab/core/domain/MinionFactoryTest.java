package com.minionslab.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.minionslab.core.common.exception.MinionException.MinionCreationException;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.event.MinionEventPublisher;
import com.minionslab.core.service.LLMService;
import com.minionslab.core.service.impl.MinionFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MinionFactoryTest {

  @Mock
  private MinionEventPublisher eventPublisher;

  @Mock
  private LLMService llmService;

  @InjectMocks
  private MinionFactory minionFactory;

  private MinionType testMinionType;
  private Map<String, Object> testMetadata;
  private MinionPrompt testPrompt;

  @BeforeEach
  void setUp() {
    testMinionType = MinionType.USER_DEFINED_AGENT;
    testMetadata = new HashMap<>();
    testMetadata.put("model", "gpt-4");
    testMetadata.put("temperature", 0.7);
    
    testPrompt = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .version("1.0")
        .description("Test prompt")
        .build();
  }

  @Test
  @DisplayName("Should create minion with all parameters")
  void createMinion_WithAllParameters_ShouldCreateMinion() {
    // Act
    Minion minion = minionFactory.createMinion(testMinionType, testMetadata, testPrompt);

    // Assert
    assertNotNull(minion);
    assertEquals(testMinionType, minion.getMinionType());
    assertEquals(testMetadata, minion.getMetadata());
    assertEquals(testPrompt, minion.getMinionPrompt());
    assertEquals(llmService, minion.getLlmService());
    assertEquals(eventPublisher, minion.getEventPublisher());
  }

  @Test
  @DisplayName("Should create minion with null prompt")
  void createMinion_WithNullPrompt_ShouldCreateMinion() {
    // Act
    Minion minion = minionFactory.createMinion(testMinionType, testMetadata, null);

    // Assert
    assertNotNull(minion);
    assertEquals(testMinionType, minion.getMinionType());
    assertEquals(testMetadata, minion.getMetadata());
    assertEquals(null, minion.getMinionPrompt());
    assertEquals(llmService, minion.getLlmService());
    assertEquals(eventPublisher, minion.getEventPublisher());
  }

  @Test
  @DisplayName("Should create minion with null metadata")
  void createMinion_WithNullMetadata_ShouldCreateMinion() {
    // Act
    Minion minion = minionFactory.createMinion(testMinionType, null, testPrompt);

    // Assert
    assertNotNull(minion);
    assertEquals(testMinionType, minion.getMinionType());
    assertNotNull(minion.getMetadata());
    assertEquals(0, minion.getMetadata().size());
    assertEquals(testPrompt, minion.getMinionPrompt());
    assertEquals(llmService, minion.getLlmService());
    assertEquals(eventPublisher, minion.getEventPublisher());
  }

  @Test
  @DisplayName("Should throw exception when minionType is null")
  void createMinion_WithNullMinionType_ShouldThrowException() {
    // Act & Assert
    assertThrows(MinionCreationException.class, () -> 
        minionFactory.createMinion(null, testMetadata, testPrompt));
  }
} 