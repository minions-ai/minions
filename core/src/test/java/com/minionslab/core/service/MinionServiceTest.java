package com.minionslab.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minionslab.core.api.dto.CreateMinionRequest;
import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionFactory;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.MinionRecipeRegistry;
import com.minionslab.core.domain.MinionRegistry;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.repository.MinionRepository;
import com.minionslab.core.test.BaseTenantAwareTest;
import com.minionslab.core.test.TestConstants;
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
  private MinionRecipeRegistry recipeRegistry;

  @Mock
  private Minion mockMinion;

  @Mock
  private MinionRepository minionRepository;

  private MinionService minionService;

  @Mock
  private PromptService promptService;

  @BeforeEach
  void setUp() {
  }

  @Test
  void createMinion_Success() throws MinionException {
    // Arrange
    Map<String, Object> metadata = new HashMap<>();
    metadata.put(TestConstants.TEST_METADATA_KEY, TestConstants.TEST_METADATA_VALUE);
    MinionPrompt prompt = MinionPrompt.builder()
        .build();

    when(minionFactory.createMinion(TestConstants.TEST_MINION_TYPE, metadata, prompt)).thenReturn(mockMinion);
    when(mockMinion.getMinionId()).thenReturn(TestConstants.TEST_PROMPT_ID);

    // Act
    Minion result = minionService.createMinion(
        CreateMinionRequest.builder().minionType(MinionType.USER_SUPPORT).promptEntityId(TestConstants.TEST_PROMPT_ID).build());

    // Assert
    assertNotNull(result);
    verify(contextService).createContext();
    verify(minionFactory).createMinion(TestConstants.TEST_MINION_TYPE, metadata, prompt);
    verify(minionRegistry).registerMinion(mockMinion);
    verify(mockMinion).setMetadata(metadata);
  }

  @Test
  void createMinion_NullType_ThrowsException() {
    // Arrange
    Map<String, Object> metadata = new HashMap<>();
    MinionPrompt prompt = MinionPrompt.builder()
        .build();

    // Act & Assert
    assertThrows(IllegalArgumentException.class,
        () -> minionService.createMinion(
            CreateMinionRequest.builder().minionType(null).promptEntityId(TestConstants.TEST_PROMPT_ID).build()));
  }

  @Test
  void createMinion_ContextCreationFails_ThrowsException() throws MinionException {
    // Arrange
    Map<String, Object> metadata = new HashMap<>();
    MinionPrompt prompt = MinionPrompt.builder()
        .build();

    doThrow(new MinionException.ContextCreationException("Context creation failed"))
        .when(contextService).createContext();

    // Act & Assert
    assertThrows(MinionException.CreationException.class,
        () -> minionService.createMinion(CreateMinionRequest.builder().minionType(MinionType.USER_SUPPORT).build()));
  }

  @Test
  void getMinionById_Success() {
    // Arrange
    when(minionRegistry.getMinionById(TestConstants.TEST_PROMPT_ID)).thenReturn(mockMinion);

    // Act
    Minion result = minionService.getMinion(TestConstants.TEST_PROMPT_ID);

    // Assert
    assertNotNull(result);
    assertEquals(mockMinion, result);
    verify(minionRegistry).getMinionById(TestConstants.TEST_PROMPT_ID);
  }
} 