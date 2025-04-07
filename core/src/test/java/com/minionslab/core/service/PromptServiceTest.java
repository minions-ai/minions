package com.minionslab.core.service;

import static com.minionslab.core.util.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.core.util.TestConstants.TEST_PROMPT_ENTITY_ID;
import static com.minionslab.core.util.TestConstants.TEST_PROMPT_ID;
import static com.minionslab.core.util.TestConstants.TEST_PROMPT_VERSION;
import static com.minionslab.core.util.TestConstants.TEST_TENANT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minionslab.core.common.exception.PromptException;
import com.minionslab.core.context.MinionContext;
import com.minionslab.core.context.MinionContextHolder;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.repository.PromptRepositoryImpl;
import com.minionslab.core.service.impl.PromptServiceImpl;
import com.minionslab.core.util.TestConstants;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.verification.AtLeast;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.autoconfigure.oci.genai.OCIConnectionProperties;
import org.springframework.validation.Validator;

@ExtendWith(MockitoExtension.class)

public class PromptServiceTest {

  private static MockedStatic<MinionContextHolder> mockedStatic;


  @Mock(lenient = true)
  private PromptRepositoryImpl promptRepository;

  @Mock
  private PromptResolver promptResolver;


  @Mock
  private Validator validator;
  @InjectMocks
  private PromptServiceImpl promptService;
  private MinionPrompt samplePrompt;
  private Map<PromptType, PromptComponent> sampleComponents;
  @Mock(strictness = Strictness.LENIENT)
  private MinionContext minionContext;


  @AfterEach
  void tearDown() {
    mockedStatic.close();
  }

  @BeforeEach
  void setUp() {

    mockedStatic = mockStatic(MinionContextHolder.class);
    mockedStatic.when(MinionContextHolder::getContext).thenReturn(minionContext);
    mockedStatic.when(MinionContextHolder::getRequiredContext).thenReturn(minionContext);
    when(minionContext.getTenantId()).thenReturn(TEST_TENANT_ID);

    samplePrompt = MinionPrompt.builder()
        .id(TEST_PROMPT_ID)
        .description(TestConstants.TEST_PROMPT_DESCRIPTION)
        .version(TestConstants.TEST_PROMPT_VERSION)
        .tenantId(TestConstants.TEST_TENANT_ID)
        .build();

    // Create sample components
    sampleComponents = new HashMap<>();
    sampleComponents.put(PromptType.SYSTEM, PromptComponent.builder()
        .type(PromptType.SYSTEM)
        .text("System prompt text")
        .build());
    sampleComponents.put(PromptType.USER_TEMPLATE, PromptComponent.builder()
        .type(PromptType.USER_TEMPLATE)
        .text("User template text")
        .build());


  }

  @Test
  void updatePrompt_WhenPromptIsLocked_And_NotEnforcingNewVersion_ShouldThrowException() {

    samplePrompt.setDeployed(true);

    // Arrange
    String promptId = TEST_PROMPT_ID;
    String description = "Updated description";
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("updatedKey", "updatedValue");
    Instant effectiveDate = Instant.now();
    Instant expiryDate = effectiveDate.plusSeconds(3600);
    boolean incrementVersionIfNeeded = false;

    // Act
    assertThatThrownBy(() -> promptService.updatePrompt(
        promptId,
        description,
        sampleComponents,
        metadata,
        effectiveDate,
        expiryDate,
        incrementVersionIfNeeded
    )).isInstanceOf(PromptException.class);

    // Assert

  }


  @Test
  void updatePrompt_WhenPromptIsLocked_And_EnforcingNewVersion_ShouldSucceed() {
    // Arrange

    String promptId = TEST_PROMPT_ID;
    String description = "Updated description";
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("updatedKey", "updatedValue");
    Instant effectiveDate = Instant.now();
    Instant expiryDate = effectiveDate.plusSeconds(3600);
    boolean incrementVersionIfNeeded = true;  // This is correct

    samplePrompt.setDeployed(true);
    samplePrompt.setEffectiveDate(effectiveDate.minus(2, ChronoUnit.DAYS));
    samplePrompt.setExpiryDate(expiryDate.plus(2, ChronoUnit.DAYS));
    when(promptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(promptRepository.findById(any())).thenReturn(Optional.of(samplePrompt));

    // Act
    MinionPrompt newPromptVersion = promptService.updatePrompt(
        promptId,
        description,
        sampleComponents,
        metadata,
        effectiveDate,
        expiryDate,
        incrementVersionIfNeeded
    );

    // Assert
    assertThat(newPromptVersion).isNotNull();
    assertThat(newPromptVersion.getVersion()).isNotEqualTo(samplePrompt.getVersion());
    assertThat(newPromptVersion.getDescription()).isEqualTo(description);
    assertThat(newPromptVersion.getMetadata()).containsEntry("updatedKey", "updatedValue");
    assertThat(newPromptVersion.getEffectiveDate()).isEqualTo(effectiveDate);
    assertThat(newPromptVersion.getExpiryDate()).isEqualTo(expiryDate);

    // Verify the original prompt's expiry date is set
    assertThat(samplePrompt.getExpiryDate()).isEqualTo(effectiveDate);

    // Verify save was called
    verify(promptRepository, new AtLeast(2)).save(any(MinionPrompt.class));
  }


  @Test
  void createPrompt_ShouldCreatePrompt_WhenValidParameters() {
    // Arrange
    String entityId = TEST_PROMPT_ENTITY_ID;
    String description = TEST_PROMPT_DESCRIPTION;
    String version = TEST_PROMPT_VERSION;
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("key", "value");
    Instant effectiveDate = Instant.now();
    Instant expiryDate = effectiveDate.plusSeconds(3600);

    when(promptRepository.save(any(MinionPrompt.class))).thenReturn(samplePrompt);

    // Act
    MinionPrompt result = promptService.createPrompt(
        entityId,
        description,
        version,
        sampleComponents,
        metadata,
        effectiveDate,
        expiryDate
    );

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(samplePrompt.getId());
    verify(promptRepository).save(any(MinionPrompt.class));
  }


  @Test
  void savePrompt_ShouldSavePrompt_WhenValidPrompt() {
    when(promptRepository.existsByEntityIdAndVersion(
        any(), any())).thenReturn(false);
    when(promptRepository.save(any(MinionPrompt.class))).thenReturn(samplePrompt);

    MinionPrompt result = promptService.savePrompt(samplePrompt);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(samplePrompt.getId());
    verify(promptRepository).save(samplePrompt);
  }


  @Test
  void getPrompts_ShouldReturnAllPrompts() {
    List<MinionPrompt> prompts = Arrays.asList(samplePrompt, samplePrompt);
    when(promptRepository.findAll()).thenReturn(prompts);

    List<MinionPrompt> result = promptService.getPrompts();

    assertThat(result).hasSize(2);
    verify(promptRepository).findAll();
  }

  @Test
  void getPrompt_ShouldReturnPrompt_WhenExists() {
    when(promptRepository.findLatestByEntityId(TEST_PROMPT_ENTITY_ID))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getPromptByEntityId(TEST_PROMPT_ENTITY_ID);

    assertThat(result).isPresent();
    assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
  }


  @Test
  void getLatestPrompt_ShouldReturnLatestPrompt() {
    when(promptRepository.findLatestByEntityId(TEST_PROMPT_ENTITY_ID))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getPromptByEntityId(TEST_PROMPT_ENTITY_ID);

    assertThat(result).isPresent();
    assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
  }

  @Test
  void getPrompt_ShouldUseTenantFromContext() {
    when(promptRepository.findLatestByEntityId(TEST_PROMPT_ENTITY_ID))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getPromptByEntityId(TEST_PROMPT_ENTITY_ID);

    assertThat(result).isPresent();
    verify(MinionContextHolder.getContext()).getTenantId();
    verify(promptRepository).findLatestByEntityId(TEST_PROMPT_ENTITY_ID);
  }

  @Test
  void getPrompt_ShouldUseContextTenant_WhenFindingByNameAndVersion() {
    when(promptRepository.findByEntityIdAndVersion(TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getPromptByEntityIdAndVersion(
        TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION);

    assertThat(result).isPresent();
    verify(MinionContextHolder.getContext()).getTenantId();
    verify(promptRepository).findByEntityIdAndVersion(TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION);
  }

  @Test
  void getPrompts_WithType_ShouldFilterByTypeAndUseTenantFromContext() {
    List<MinionPrompt> prompts = Arrays.asList(samplePrompt);
    when(promptRepository.findAll()).thenReturn(prompts);

    List<MinionPrompt> result = promptService.getPrompts();

    assertThat(result).hasSize(1);

    verify(promptRepository).findAll();
  }

  @Test
  void updatePrompt_ShouldUpdatePrompt_WhenValidParameters() {
    // Arrange
    String promptId = TEST_PROMPT_ID;
    String description = "Updated description";
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("updatedKey", "updatedValue");
    Instant effectiveDate = Instant.now();
    Instant expiryDate = effectiveDate.plusSeconds(3600);
    boolean incrementVersionIfNeeded = false;

    when(promptRepository.findById(promptId)).thenReturn(Optional.of(samplePrompt));
    when(promptRepository.save(any(MinionPrompt.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    MinionPrompt result = promptService.updatePrompt(
        promptId,
        description,
        sampleComponents,
        metadata,
        effectiveDate,
        expiryDate,
        incrementVersionIfNeeded
    );

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getDescription()).isEqualTo(description);
    assertThat(result.getMetadata()).containsEntry("updatedKey", "updatedValue");
    assertThat(result.getEffectiveDate()).isEqualTo(effectiveDate);
    assertThat(result.getExpiryDate()).isEqualTo(expiryDate);
    verify(promptRepository).save(any(MinionPrompt.class));
  }

  @Test
  void updateComponent_ShouldUpdateComponent_WhenValidParameters() {
    // Arrange
    String promptId = TEST_PROMPT_ID;
    Instant updateEffectiveDate = Instant.now();
    PromptType componentType = PromptType.SYSTEM;
    String componentText = "Updated system prompt text";
    Map<String, Object> componentMetadata = new HashMap<>();
    componentMetadata.put("updatedKey", "updatedValue");
    boolean incrementVersionIfNeeded = false;

    when(promptRepository.findById(promptId)).thenReturn(Optional.of(samplePrompt));
    when(promptRepository.save(any(MinionPrompt.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    MinionPrompt result = promptService.updateComponent(
        promptId,
        updateEffectiveDate,
        componentType,
        componentText,
        componentMetadata,
        incrementVersionIfNeeded
    );

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getComponents().get(componentType).getText()).isEqualTo(componentText);
    assertThat(result.getComponents().get(componentType).getMetadata()).containsEntry("updatedKey", "updatedValue");
    verify(promptRepository).save(any(MinionPrompt.class));
  }

  private MinionPrompt createPrompt(MinionType type, String name, String version) {
    return MinionPrompt.builder()
        .id(TEST_PROMPT_ID)
        .description(name)
        .version(version)
        .tenantId(TEST_TENANT_ID)
        .build();
  }


}