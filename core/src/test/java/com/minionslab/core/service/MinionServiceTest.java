package com.minionslab.core.service;

import static com.minionslab.core.util.TestConstants.TEST_MINION_ID;
import static com.minionslab.core.util.TestConstants.TEST_PROMPT_ENTITY_ID;
import static com.minionslab.core.util.TestConstants.TEST_TENANT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.common.util.ContextUtils;
import com.minionslab.core.context.MinionContext;
import com.minionslab.core.context.MinionContextHolder;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.MinionRecipeRegistry;
import com.minionslab.core.domain.MinionRegistry;
import com.minionslab.core.domain.enums.MinionState;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.event.MinionEventPublisher;
import com.minionslab.core.repository.MinionRepository;
import com.minionslab.core.repository.PromptRepository;
import com.minionslab.core.service.impl.MinionFactory;
import com.minionslab.core.service.impl.MinionServiceImpl;
import com.minionslab.core.service.impl.llm.LLMServiceFactory;
import com.minionslab.core.service.impl.llm.model.LLMResponse;
import com.minionslab.core.util.TestConstants;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MinionServiceTest {


  protected static MockedStatic<MinionContextHolder> mockedStatic;
  @Mock(strictness = Strictness.LENIENT)
  protected MinionContext minionContext;

  @Mock
  private MinionRepository minionRepository;
  @Mock
  private PromptRepository promptRepository;
  @Mock
  private MinionFactory minionFactory;
  @Mock(strictness = Strictness.LENIENT)
  private MinionRegistry minionRegistry;
  @Mock
  private MinionRecipeRegistry recipeRegistry;
  @Mock
  private PromptService promptService;
  @Mock(strictness = Strictness.LENIENT)
  private LLMServiceFactory llmServiceFactory;

  @Mock(strictness = Strictness.LENIENT)
  private LLMService llmService;

  private ContextUtils contextUtils;
  private MinionService minionService;

  private MinionPrompt samplePrompt;
  private Minion sampleMinion;
  private Map<String, Object> sampleMetadata;
  @Mock
  private MinionCreationService minionCreationService;

  @Mock
  private MinionEventPublisher minionEventPublisher;
  @Mock(strictness = Strictness.LENIENT)
  private LLMResponse llmResponse;

  @AfterEach
  public void tearDown() {
    mockedStatic.close();
  }

  @BeforeEach
  void setUp() {
    mockedStatic = mockStatic(MinionContextHolder.class);
    mockedStatic.when(MinionContextHolder::getContext).thenReturn(minionContext);
    mockedStatic.when(MinionContextHolder::getRequiredContext).thenReturn(minionContext);
    when(minionContext.getTenantId()).thenReturn(TEST_TENANT_ID);

    minionService = new MinionServiceImpl(minionFactory, minionRegistry, recipeRegistry, minionRepository, promptRepository, promptService,
        llmServiceFactory, minionCreationService);

    when(llmServiceFactory.getLLMService()).thenReturn(llmService);

    when(llmService.processRequest(any())).thenReturn(llmResponse);

    when(llmResponse.getResponseText()).thenReturn("Processed response for minion: " + TEST_MINION_ID);

    when(minionContext.getTenantId()).thenReturn(TEST_TENANT_ID);

    samplePrompt = MinionPrompt.builder()
        .id(TestConstants.TEST_PROMPT_ID)
        .description(TestConstants.TEST_PROMPT_DESCRIPTION)
        .version(TestConstants.TEST_PROMPT_VERSION)
        .tenantId(TestConstants.TEST_TENANT_ID)
        .build();

    sampleMinion = Minion.builder()
        .id(TEST_MINION_ID)
        .minionType(MinionType.USER_DEFINED_AGENT)
        .prompt(samplePrompt)
        .tenantId(TestConstants.TEST_TENANT_ID)
        .state(MinionState.IDLE)
        .eventPublisher(minionEventPublisher)
        .llmService(llmService)
        .build();

    when(minionRegistry.getMinion(eq(TEST_MINION_ID))).thenReturn(sampleMinion);

    sampleMetadata = new HashMap<>();
    sampleMetadata.put("key", "value");
  }

  @Test
  void createMinion_ShouldCreateMinion_WhenValidParameters() {
    // Arrange
    MinionType minionType = MinionType.USER_DEFINED_AGENT;
    String promptEntityId = TEST_PROMPT_ENTITY_ID;
    Instant effectiveDate = Instant.now();
    Instant expiryDate = effectiveDate.plusSeconds(3600);

    when(promptService.getPrompt(promptEntityId))
        .thenReturn(Optional.of(samplePrompt));

    when(minionCreationService.createMinion(any(), any(), any())).thenReturn(sampleMinion);

    // Act
    Minion result = minionService.createMinion(
        minionType,
        sampleMetadata,
        promptEntityId,
        effectiveDate,
        expiryDate
    );

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getTenantId()).isEqualTo(TEST_TENANT_ID);
    assertThat(result.getId()).isEqualTo(sampleMinion.getId());
    verify(minionRepository).save(any(Minion.class));
  }

  @Test
  void createMinion_ShouldThrowException_WhenPromptNotFound() {
    // Arrange
    MinionType minionType = MinionType.USER_DEFINED_AGENT;
    String promptEntityId = "nonexistent-prompt";
    Instant effectiveDate = Instant.now();
    Instant expiryDate = effectiveDate.plusSeconds(3600);

    // Act & Assert
    assertThatThrownBy(() -> minionService.createMinion(
        minionType,
        sampleMetadata,
        promptEntityId,
        effectiveDate,
        expiryDate
    ))
        .isInstanceOf(MinionException.class)
        .hasMessageContaining("Prompt not found");
  }


  @Test
  void processRequest_ShouldProcessRequest_WhenValidParameters() {
    // Arrange
    String minionId = "test-minion-id";
    String request = "Test request";
    Map<String, Object> context = new HashMap<>();
    context.put("key", "value");

    // Act
    String result = minionService.processRequest(minionId, request, context);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo("Processed response for minion: " + minionId);
  }

  @Test
  void processRequest_ShouldThrowException_WhenMinionNotFound() {
    // Arrange
    String minionId = "nonexistent-minion";
    String request = "Test request";
    Map<String, Object> context = new HashMap<>();

    when(minionRegistry.getMinion(any())).thenThrow(new MinionException("Minion not found"));
    // Act & Assert
    assertThatThrownBy(() -> minionService.processRequest(minionId, request, context))
        .isInstanceOf(MinionException.class)
        .hasMessageContaining("Minion not found");
  }


}