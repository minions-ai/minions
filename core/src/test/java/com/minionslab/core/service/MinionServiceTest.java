package com.minionslab.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionFactory;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.MinionRegistry;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.test.BaseTenantAwareTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MinionServiceTest extends BaseTenantAwareTest {

  @Mock
  private MinionFactory minionFactory;

  @Mock
  private MinionRegistry minionRegistry;

  @Mock
  private ContextService contextService;

  @Mock
  private Minion mockMinion;

  private MinionService minionService;

  @BeforeEach
  void setUp() {
    minionService = new MinionService(minionFactory, minionRegistry, contextService);
  }

  @Test
  void createMinion_Success() throws MinionException {
    // Arrange
    MinionType type = MinionType.USER_DEFINED_AGENT;
    Map<String, String> metadata = new HashMap<>();
    metadata.put("key", "value");
    MinionPrompt prompt = MinionPrompt.builder()
        .build();
    prompt.addContent("This is a prompt");

    when(minionFactory.createMinion(type, metadata, prompt)).thenReturn(mockMinion);
    when(mockMinion.getMinionId()).thenReturn("test-id");

    // Act
    Minion result = minionService.createMinion(type, metadata, prompt);

    // Assert
    assertNotNull(result);
    verify(contextService).createContext();
    verify(minionFactory).createMinion(type, metadata, prompt);
    verify(minionRegistry).registerAgent(mockMinion);
    verify(mockMinion).setMetadata(metadata);
  }

  @Test
  void createMinion_NullType_ThrowsException() {
    // Arrange
    Map<String, String> metadata = new HashMap<>();
    MinionPrompt prompt = MinionPrompt.builder()
        .build();
    prompt.addContent("This is a prompt");

    // Act & Assert
    assertThrows(IllegalArgumentException.class,
        () -> minionService.createMinion(null, metadata, prompt));
  }

  @Test
  void createMinion_ContextCreationFails_ThrowsException() throws MinionException {
    // Arrange
    MinionType type = MinionType.valueOf("SOME_TYPE");
    Map<String, String> metadata = new HashMap<>();
    MinionPrompt prompt = MinionPrompt.builder()
        .build();
    prompt.addContent("This is a prompt");

    doThrow(new MinionException.ContextCreationException("Context creation failed"))
        .when(contextService).createContext();

    // Act & Assert
    assertThrows(MinionException.CreationException.class,
        () -> minionService.createMinion(type, metadata, prompt));
  }

  @Test
  void getMinionById_Success() {
    // Arrange
    String minionId = "test-id";
    when(minionRegistry.getMinionById(minionId)).thenReturn(mockMinion);

    // Act
    Minion result = minionService.getMinionById(minionId);

    // Assert
    assertNotNull(result);
    assertEquals(mockMinion, result);
    verify(minionRegistry).getMinionById(minionId);
  }
} 