package com.minionslab.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minionslab.core.common.exception.PromptException;
import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionContextHolder;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.repository.PromptRepository;
import com.minionslab.core.repository.validator.PromptValidator;
import com.minionslab.core.service.resolver.PromptResolver;
import com.minionslab.core.test.BaseTenantAwareTest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest extends BaseTenantAwareTest {

  public static final String USER_ID = "user123";
  private static final String TENANT_ID = "tenant123";
  private static final String PROMPT_NAME = "testPrompt";
  private static final String PROMPT_VERSION = "1.0";
  private static final MinionType PROMPT_TYPE = MinionType.USER_DEFINED_AGENT;
  @Mock
  private PromptRepository promptRepository;
  @Mock
  private PromptValidator promptValidator;
  @Mock
  private PromptResolver promptResolver;
  @Mock(lenient = true)
  private MinionContext minionContext;
  private PromptService promptService;
  private MockedStatic<MinionContextHolder> mockedStatic;
  private MinionPrompt samplePrompt;

  @BeforeEach
  void setUp() {
    // Mock the static MinionContextHolder


    promptService = new PromptService(promptRepository, promptValidator, promptResolver);

    samplePrompt = MinionPrompt.builder()
        .id("prompt123")
        .name(PROMPT_NAME)
        .version(PROMPT_VERSION)
        .type(PROMPT_TYPE)
        .tenantId(TENANT_ID)
        .build();
  }

  @AfterEach
  void tearDown() {
    // Close the static mock to prevent memory leaks
    mockedStatic.close();
  }

  @Test
  void savePrompt_ShouldSavePrompt_WhenValidPrompt() {
    when(promptRepository.existsByTypeAndNameAndVersionAndTenantId(
        any(), any(), any(), any())).thenReturn(false);
    when(promptRepository.save(any(MinionPrompt.class))).thenReturn(samplePrompt);

    MinionPrompt result = promptService.savePrompt(samplePrompt);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(samplePrompt.getId());
    verify(promptRepository).save(samplePrompt);
  }

  @Test
  void savePrompt_ShouldThrowException_WhenDuplicateVersion() {
    when(promptRepository.existsByTypeAndNameAndVersionAndTenantId(
        any(), any(), any(), any())).thenReturn(true);

    assertThatThrownBy(() -> promptService.savePrompt(samplePrompt))
        .isInstanceOf(PromptException.DuplicatePromptException.class)
        .hasMessageContaining("Prompt already exists with version");
  }

  @Test
  void savePrompt_ShouldThrowException_WhenInvalidPrompt() {
    samplePrompt.setName(null);

    assertThatThrownBy(() -> promptService.savePrompt(samplePrompt))
        .isInstanceOf(PromptException.InvalidPromptException.class)
        .hasMessageContaining("Prompt name cannot be empty");
  }

  @Test
  void getPrompts_ShouldReturnAllPrompts() {
    List<MinionPrompt> prompts = Arrays.asList(samplePrompt, samplePrompt);
    when(promptRepository.findAllByTenantId(null)).thenReturn(prompts);

    List<MinionPrompt> result = promptService.getPrompts();

    assertThat(result).hasSize(2);
    verify(promptRepository).findAllByTenantId(null);
  }

  @Test
  void getPrompt_ShouldReturnPrompt_WhenExists() {
    when(promptRepository.findLatestByTypeAndNameAndTenantId(
        PROMPT_TYPE, PROMPT_NAME, TENANT_ID))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getPrompt(PROMPT_TYPE, PROMPT_NAME);

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo(PROMPT_NAME);
  }

  @Test
  void getPromptsByType_ShouldReturnPromptsByType() {
    List<MinionPrompt> prompts = Arrays.asList(samplePrompt, samplePrompt);
    when(promptRepository.findAllByTypeAndTenantId(PROMPT_TYPE, TENANT_ID))
        .thenReturn(prompts);

    List<MinionPrompt> result = promptService.getPromptsByType(PROMPT_TYPE, TENANT_ID);

    assertThat(result).hasSize(2);
    verify(promptRepository).findAllByTypeAndTenantId(PROMPT_TYPE, TENANT_ID);
  }

  @Test
  void deletePrompt_ShouldDeletePrompt_WhenExists() {
    when(promptRepository.findById("prompt123")).thenReturn(Optional.of(samplePrompt));

    promptService.deletePrompt("prompt123");

    verify(promptRepository).deleteById("prompt123");
  }

  @Test
  void deletePrompt_ShouldThrowException_WhenNotExists() {
    when(promptRepository.findById("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> promptService.deletePrompt("nonexistent"))
        .isInstanceOf(PromptException.PromptNotFoundException.class)
        .hasMessageContaining("Prompt not found");
  }

  @Test
  void getLatestPrompt_ShouldReturnLatestPrompt() {
    when(promptRepository.findLatestByTypeAndNameAndTenantId(
        PROMPT_TYPE, PROMPT_NAME, TENANT_ID))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getLatestPrompt(
        PROMPT_TYPE, PROMPT_NAME, TENANT_ID);

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo(PROMPT_NAME);
  }

  @Test
  void validateRequiredFields_ShouldThrowException_WhenTypeIsNull() {
    MinionPrompt invalidPrompt = MinionPrompt.builder()
        .name(PROMPT_NAME)
        .build();

    assertThatThrownBy(() -> promptService.validateRequiredFields(invalidPrompt))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Agent minionType is required");
  }

  @Test
  void validateRequiredFields_ShouldThrowException_WhenNameIsEmpty() {
    MinionPrompt invalidPrompt = MinionPrompt.builder()
        .type(PROMPT_TYPE)
        .build();

    assertThatThrownBy(() -> promptService.validateRequiredFields(invalidPrompt))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Name is required");
  }

  @Test
  void getPrompt_ShouldUseTenantFromContext() {
    when(promptRepository.findLatestByTypeAndNameAndTenantId(
        PROMPT_TYPE, PROMPT_NAME, TENANT_ID))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getPrompt(PROMPT_TYPE, PROMPT_NAME);

    assertThat(result).isPresent();
    verify(minionContext).getTenantId();
    verify(promptRepository).findLatestByTypeAndNameAndTenantId(PROMPT_TYPE, PROMPT_NAME, TENANT_ID);
  }

  @Test
  void getPrompt_ShouldUseContextTenant_WhenFindingByNameAndVersion() {
    when(promptRepository.findByNameAndVersionAndTenantId(
        PROMPT_NAME, PROMPT_VERSION, TENANT_ID))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getPrompt(PROMPT_NAME, PROMPT_VERSION);

    assertThat(result).isPresent();
    verify(minionContext).getTenantId();
    verify(promptRepository).findByNameAndVersionAndTenantId(PROMPT_NAME, PROMPT_VERSION, TENANT_ID);
  }

  @Test
  void getPrompts_WithType_ShouldFilterByTypeAndUseTenantFromContext() {
    List<MinionPrompt> allPrompts = Arrays.asList(
        samplePrompt,
        createPrompt(PROMPT_TYPE, "other", "1.0"),
        createPrompt(MinionType.USER_DEFINED_AGENT, "test", "1.0")
    );

    when(promptRepository.findAllByTenantId(null)).thenReturn(allPrompts);

    List<MinionPrompt> result = promptService.getPrompts(PROMPT_TYPE);

    assertThat(result)
        .hasSize(3)
        .allMatch(prompt -> prompt.getType() == PROMPT_TYPE);
  }

  // Helper method to create test prompts
  private MinionPrompt createPrompt(MinionType type, String name, String version) {
    MinionPrompt prompt = MinionPrompt.builder()
        .type(type)
        .name(name)
        .version(version)
        .tenantId(TENANT_ID)
        .build();
    return prompt;
  }
} 