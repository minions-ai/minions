package com.minionslab.core.repository;

import static com.minionslab.core.util.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.core.util.TestConstants.TEST_PROMPT_ENTITY_ID;
import static com.minionslab.core.util.TestConstants.TEST_PROMPT_ID;
import static com.minionslab.core.util.TestConstants.TEST_PROMPT_VERSION;
import static com.minionslab.core.util.TestConstants.TEST_TENANT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.minionslab.core.context.MinionContext;
import com.minionslab.core.context.MinionContextHolder;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Test class for PromptRepository. This class is part of the ContextAwareTestSuite and relies on the suite's static mocking setup.
 */
@ExtendWith(MockitoExtension.class)

public class PromptRepositoryTest extends BaseMongoTest {

  private static MockedStatic<MinionContextHolder> mockedStatic;


  private PromptRepositoryImpl repository;

  private MinionPrompt samplePrompt;

  @Mock
  private MinionContext minionContext;


  @BeforeEach
  @Override
  void setUp() {
    super.setUp();

    mockedStatic = Mockito.mockStatic(MinionContextHolder.class);
    mockedStatic.when(MinionContextHolder::getContext).thenReturn(minionContext);
    mockedStatic.when(MinionContextHolder::getRequiredContext).thenReturn(minionContext);
    when(minionContext.getTenantId()).thenReturn(TEST_TENANT_ID);
    // Create the repository with the mongoTemplate and MinionContextHolder
    // We create a new instance of MinionContextHolder since the static methods are being mocked by ContextAwareTestSuite
    repository = new PromptRepositoryImpl(mongoTemplate);

    // Create a sample prompt
    samplePrompt = MinionPrompt.builder()
        .id(TEST_PROMPT_ID)
        .description(TEST_PROMPT_DESCRIPTION)
        .version(TEST_PROMPT_VERSION)
        .tenantId(TEST_TENANT_ID)
        .entityId(TEST_PROMPT_ENTITY_ID)
        .effectiveDate(Instant.now())
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("Test system prompt")
            .build()))
        .build();

    repository.save(samplePrompt);
  }

  @AfterEach
  void tearDown() {
    mockedStatic.close();
  }

  @Test
  void findActiveVersion_ShouldReturnActivePrompt() {
    // Arrange
    Instant now = Instant.now();

    // Act
    Optional<MinionPrompt> result = repository.findActiveVersion(TEST_PROMPT_ENTITY_ID, now);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
    assertThat(result.get().getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    assertThat(result.get().getTenantId()).isEqualTo(TEST_TENANT_ID);
  }

  @Test
  void findActiveVersion_ShouldNotReturnExpiredPrompt() {
    // Arrange
    MinionPrompt expiredPrompt = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .description("Expired prompt")
        .version("2.0")
        .tenantId(TEST_TENANT_ID)
        .entityId(TEST_PROMPT_ENTITY_ID)
        .effectiveDate(Instant.now().minusSeconds(3600))
        .expiryDate(Instant.now().minusSeconds(1800))
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("Expired system prompt")
            .build()))
        .build();

    repository.save(expiredPrompt);
    Instant now = Instant.now();

    // Act
    Optional<MinionPrompt> result = repository.findActiveVersion(TEST_PROMPT_ENTITY_ID, now);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
    assertThat(result.get().getVersion()).isEqualTo(TEST_PROMPT_VERSION);
  }

  @Test
  void findActiveVersion_ShouldReturnLatestActiveVersion() {
    // Arrange
    MinionPrompt newerPrompt = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .description("Newer prompt")
        .version("2.0")
        .tenantId(TEST_TENANT_ID)
        .entityId(TEST_PROMPT_ENTITY_ID)
        .effectiveDate(Instant.now().plusSeconds(3600))
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("Newer system prompt")
            .build()))
        .build();

    repository.save(newerPrompt);
    Instant now = Instant.now().plusSeconds(7200);

    // Act
    Optional<MinionPrompt> result = repository.findActiveVersion(TEST_PROMPT_ENTITY_ID, now);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDescription()).isEqualTo("Newer prompt");
    assertThat(result.get().getVersion()).isEqualTo("2.0");
  }

  @Test
  void findAllByEntityIdAfterOrderByEffectiveDateDesc_ShouldReturnAllVersions() {
    // Arrange
    MinionPrompt olderPrompt = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .description("Older prompt")
        .version("0.5")
        .tenantId(TEST_TENANT_ID)
        .entityId(TEST_PROMPT_ENTITY_ID)
        .effectiveDate(Instant.now().minusSeconds(3600))
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("Older system prompt")
            .build()))
        .build();

    repository.save(olderPrompt);

    // Act
    List<MinionPrompt> results = repository.findAllByEntityIdAfterOrderByEffectiveDateDesc(TEST_PROMPT_ENTITY_ID);

    // Assert
    assertThat(results).hasSize(2);
    assertThat(results.get(0).getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    assertThat(results.get(1).getVersion()).isEqualTo("0.5");
  }

  @Test
  void findByEntityIdAndVersion_ShouldReturnPrompt() {
    // Act
    Optional<MinionPrompt> result = repository.findByEntityIdAndVersion(TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
    assertThat(result.get().getVersion()).isEqualTo(TEST_PROMPT_VERSION);
  }

  @Test
  void findLatestByEntityId_ShouldReturnLatestPrompt() {
    // Arrange
    MinionPrompt newerPrompt = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .description("Newer prompt")
        .version("2.0")
        .tenantId(TEST_TENANT_ID)
        .entityId(TEST_PROMPT_ENTITY_ID)
        .effectiveDate(Instant.now().plusSeconds(3600))
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("Newer system prompt")
            .build()))
        .build();

    repository.save(newerPrompt);

    // Act
    Optional<MinionPrompt> result = repository.findLatestByEntityId(TEST_PROMPT_ENTITY_ID);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDescription()).isEqualTo("Newer prompt");
    assertThat(result.get().getVersion()).isEqualTo("2.0");
  }

  @Test
  void findAllByEntityId_ShouldReturnAllVersions() {
    // Arrange
    MinionPrompt olderPrompt = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .description("Older prompt")
        .version("0.5")
        .tenantId(TEST_TENANT_ID)
        .entityId(TEST_PROMPT_ENTITY_ID)
        .effectiveDate(Instant.now().minusSeconds(3600))
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("Older system prompt")
            .build()))
        .build();

    repository.save(olderPrompt);

    // Act
    List<MinionPrompt> results = repository.findAllByEntityId(TEST_PROMPT_ENTITY_ID);

    // Assert
    assertThat(results).hasSize(2);
    assertThat(results.get(0).getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    assertThat(results.get(1).getVersion()).isEqualTo("0.5");
  }

  @Test
  void existsByEntityIdAndVersion_ShouldReturnTrue_WhenExists() {
    // Act
    boolean result = repository.existsByEntityIdAndVersion(TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION);

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  void existsByEntityIdAndVersion_ShouldReturnFalse_WhenNotExists() {
    // Act
    boolean result = repository.existsByEntityIdAndVersion(TEST_PROMPT_ENTITY_ID, "non-existent");

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  void findAllVersions_ShouldReturnAllVersions() {
    // Arrange
    MinionPrompt olderPrompt = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .description("Older prompt")
        .version("0.5")
        .tenantId(TEST_TENANT_ID)
        .entityId(TEST_PROMPT_ENTITY_ID)
        .effectiveDate(Instant.now().minusSeconds(3600))
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("Older system prompt")
            .build()))
        .build();

    repository.save(olderPrompt);

    // Act
    List<MinionPrompt> results = repository.findAllVersions(TEST_PROMPT_ENTITY_ID);

    // Assert
    assertThat(results).hasSize(2);
    assertThat(results.get(0).getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    assertThat(results.get(1).getVersion()).isEqualTo("0.5");
  }

  @Test
  void findById_ShouldReturnPrompt() {
    // Act
    Optional<MinionPrompt> result = repository.findById(TEST_PROMPT_ID);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
    assertThat(result.get().getVersion()).isEqualTo(TEST_PROMPT_VERSION);
  }

  @Test
  void deleteById_ShouldDeletePrompt() {
    // Act
    repository.deleteById(TEST_PROMPT_ID);

    // Assert
    assertThat(repository.findById(TEST_PROMPT_ID)).isEmpty();
  }

  @Test
  void findAll_ShouldReturnAllPrompts() {
    // Act
    List<MinionPrompt> results = repository.findAll();

    // Assert
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
  }

  @Test
  void findAll_WithSort_ShouldReturnSortedPrompts() {
    // Arrange
    MinionPrompt olderPrompt = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .description("Older prompt")
        .version("0.5")
        .tenantId(TEST_TENANT_ID)
        .entityId(TEST_PROMPT_ENTITY_ID)
        .effectiveDate(Instant.now().minusSeconds(3600))
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("Older system prompt")
            .build()))
        .build();

    repository.save(olderPrompt);

    // Act
    List<MinionPrompt> results = repository.findAll(Sort.by(Sort.Direction.DESC, "version"));

    // Assert
    assertThat(results).hasSize(2);
    assertThat(results.get(0).getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    assertThat(results.get(1).getVersion()).isEqualTo("0.5");
  }

  @Test
  void findAll_WithPageable_ShouldReturnCorrectTotalPages() {
    // Arrange
    MinionPrompt olderPrompt = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .description("Older prompt")
        .version("0.5")
        .tenantId(TEST_TENANT_ID)
        .entityId(TEST_PROMPT_ENTITY_ID)
        .effectiveDate(Instant.now().minusSeconds(3600))
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("Older system prompt")
            .build()))
        .build();

    repository.save(olderPrompt);

    // Act
    Page<MinionPrompt> page1 = repository.findAll(PageRequest.of(0, 1));
    Page<MinionPrompt> page2 = repository.findAll(PageRequest.of(1, 1));

    // Assert
    assertThat(page1.getTotalElements()).isEqualTo(2);
    assertThat(page1.getTotalPages()).isEqualTo(2);
    assertThat(page1.getContent()).hasSize(1);
    assertThat(page1.getContent().get(0).getVersion()).isEqualTo(TEST_PROMPT_VERSION);

    assertThat(page2.getTotalElements()).isEqualTo(2);
    assertThat(page2.getTotalPages()).isEqualTo(2);
    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).getVersion()).isEqualTo("0.5");
  }

  @Test
  void save_ShouldSetTenantId_WhenNotSet() {
    // Arrange
    MinionPrompt promptWithoutTenant = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .description("Prompt without tenant")
        .version("1.0")
        .entityId(UUID.randomUUID().toString())
        .effectiveDate(Instant.now())
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("System prompt")
            .build()))
        .build();

    // Act
    MinionPrompt savedPrompt = repository.save(promptWithoutTenant);

    // Assert
    assertThat(savedPrompt.getTenantId()).isEqualTo(TEST_TENANT_ID);
  }

  @Test
  void saveAll_ShouldSetTenantId_WhenNotSet() {
    // Arrange
    MinionPrompt prompt1WithoutTenant = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .description("Prompt 1 without tenant")
        .version("1.0")
        .entityId(UUID.randomUUID().toString())
        .effectiveDate(Instant.now())
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("System prompt 1")
            .build()))
        .build();

    MinionPrompt prompt2WithoutTenant = MinionPrompt.builder()
        .id(UUID.randomUUID().toString())
        .description("Prompt 2 without tenant")
        .version("1.0")
        .entityId(UUID.randomUUID().toString())
        .effectiveDate(Instant.now())
        .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
            .type(PromptType.SYSTEM)
            .text("System prompt 2")
            .build()))
        .build();

    // Act
    List<MinionPrompt> savedPrompts = repository.saveAll(List.of(prompt1WithoutTenant, prompt2WithoutTenant));

    // Assert
    assertThat(savedPrompts).hasSize(2);
    assertThat(savedPrompts.get(0).getTenantId()).isEqualTo(TEST_TENANT_ID);
    assertThat(savedPrompts.get(1).getTenantId()).isEqualTo(TEST_TENANT_ID);
  }
}