package com.minionslab.core.service;

import static com.minionslab.core.test.TestConstants.TEST_MINION_TYPE;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_ENTITY_ID;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_VERSION;
import static com.minionslab.core.test.TestConstants.TEST_TENANT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minionslab.core.common.exception.PromptException;
import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionContextHolder;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.repository.PromptRepository;
import com.minionslab.core.test.BaseTenantAwareTest;
import com.minionslab.core.test.TestConstants;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PromptServiceTest extends BaseTenantAwareTest {

  private static MockedStatic<MinionContextHolder> mockedStatic;
  @Mock(lenient = true)
  private PromptRepository promptRepository;

  @Mock
  private PromptResolver promptResolver;
  @Mock(lenient = true)
  private MinionContext minionContext;
  @Autowired
  private PromptService promptService;
  private MinionPrompt samplePrompt;

  @BeforeEach
  void setUp() {
    samplePrompt = MinionPrompt.builder()
        .id(TestConstants.TEST_PROMPT_ID)
        .description(TestConstants.TEST_PROMPT_DESCRIPTION)
        .version(TestConstants.TEST_PROMPT_VERSION)
        .tenantId(TestConstants.TEST_TENANT_ID)
        .build();
  }

  @Test
  void savePrompt_ShouldSavePrompt_WhenValidPrompt() {
    when(promptRepository.existsByEntityIdAndVersionAndTenantId(
        any(), any(), any())).thenReturn(false);
    when(promptRepository.save(any(MinionPrompt.class))).thenReturn(samplePrompt);

    MinionPrompt result = promptService.savePrompt(samplePrompt);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(samplePrompt.getId());
    verify(promptRepository).save(samplePrompt);
  }

  @Test
  void savePrompt_ShouldThrowException_WhenDuplicateVersion() {
    when(promptRepository.existsByEntityIdAndVersionAndTenantId(
        any(), any(), any())).thenReturn(true);

    assertThatThrownBy(() -> promptService.savePrompt(samplePrompt))
        .isInstanceOf(PromptException.DuplicatePromptException.class)
        .hasMessageContaining("Prompt already exists with version");
  }

  @Test
  void savePrompt_ShouldThrowException_WhenInvalidPrompt() {
    samplePrompt.setDescription(null);

    assertThatThrownBy(() -> promptService.savePrompt(samplePrompt))
        .isInstanceOf(PromptException.InvalidPromptException.class)
        .hasMessageContaining("Prompt description cannot be empty");
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
    when(promptRepository.findLatestByEntityIdAndTenantId(
        TEST_PROMPT_ENTITY_ID, MinionContextHolder.getContext().getTenantId()))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getPromptByEntityId(TEST_PROMPT_ENTITY_ID);

    assertThat(result).isPresent();
    assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
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
    when(promptRepository.findLatestByEntityIdAndTenantId(
        TEST_PROMPT_ENTITY_ID, TEST_TENANT_ID))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getPromptByEntityId(TEST_PROMPT_ENTITY_ID);

    assertThat(result).isPresent();
    assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
  }

  @Test
  void getPrompt_ShouldUseTenantFromContext() {
    when(promptRepository.findLatestByEntityIdAndTenantId(
        TEST_PROMPT_ENTITY_ID, TEST_TENANT_ID))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getPromptByEntityId(TEST_PROMPT_ENTITY_ID);

    assertThat(result).isPresent();
    verify(MinionContextHolder.getContext()).getTenantId();
    verify(promptRepository).findLatestByEntityIdAndTenantId(TEST_PROMPT_ENTITY_ID, TEST_TENANT_ID);
  }

  @Test
  void getPrompt_ShouldUseContextTenant_WhenFindingByNameAndVersion() {
    when(promptRepository.findByEntityIdAndVersionAndTenantId(
        TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION, TEST_TENANT_ID))
        .thenReturn(Optional.of(samplePrompt));

    Optional<MinionPrompt> result = promptService.getPromptByEntityIdAndVersion(TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION);

    assertThat(result).isPresent();
    verify(MinionContextHolder.getContext()).getTenantId();
    verify(promptRepository).findByEntityIdAndVersionAndTenantId(TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION, TEST_TENANT_ID);
  }

  @Test
  void getPrompts_WithType_ShouldFilterByTypeAndUseTenantFromContext() {
    List<MinionPrompt> allPrompts = Arrays.asList(
        samplePrompt,
        createPrompt(TEST_MINION_TYPE, "other", "1.0"),
        createPrompt(MinionType.USER_DEFINED_AGENT, "test", "1.0")
    );

    when(promptRepository.findAllByTenantId(null)).thenReturn(allPrompts);

    List<MinionPrompt> result = promptService.getPrompts();

    assertThat(result)
        .hasSize(3);
  }

  // Helper method to create test prompts
  private MinionPrompt createPrompt(MinionType type, String name, String version) {
    MinionPrompt prompt = MinionPrompt.builder()
        .description(TEST_PROMPT_DESCRIPTION)
        .version(version)
        .tenantId(TEST_TENANT_ID)
        .build();
    return prompt;
  }
} 