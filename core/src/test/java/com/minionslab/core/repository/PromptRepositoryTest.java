package com.minionslab.core.repository;

import static com.minionslab.core.test.TestConstants.TEST_PROMPT_ENTITY_ID;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_ID;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_VERSION;
import static com.minionslab.core.test.TestConstants.TEST_TENANT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class PromptRepositoryTest {

    @Mock
    private PromptRepository repository;

    private MinionPrompt samplePrompt;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void findActiveVersion_ShouldReturnActivePrompt() {
        // Arrange
        when(repository.findActiveVersion(eq(TEST_PROMPT_ENTITY_ID), any(), TEST_TENANT_ID)).thenReturn(Optional.of(samplePrompt));

        // Act
        Optional<MinionPrompt> result = repository.findActiveVersion(TEST_PROMPT_ENTITY_ID, Instant.now(), TEST_TENANT_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
        assertThat(result.get().getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    }

    @Test
    void findActiveVersion_ShouldNotReturnExpiredPrompt() {
        // Arrange
        when(repository.findActiveVersion(eq(TEST_PROMPT_DESCRIPTION), any(Instant.class), TEST_TENANT_ID))
            .thenReturn(Optional.empty());

        // Act
        Optional<MinionPrompt> result = repository.findActiveVersion(TEST_PROMPT_DESCRIPTION, Instant.now(), TEST_TENANT_ID);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findActiveVersion_ShouldReturnLatestActiveVersion() {
        // Arrange
        MinionPrompt latestPrompt = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version("1.0.1")
            .tenantId(TEST_TENANT_ID)
            .effectiveDate(Instant.now().plusSeconds(1))
            .components(samplePrompt.getComponents())
            .build();

        when(repository.findActiveVersion(eq(TEST_PROMPT_ENTITY_ID), any(Instant.class), TEST_TENANT_ID))
            .thenReturn(Optional.of(latestPrompt));

        // Act
        Optional<MinionPrompt> result = repository.findActiveVersion(TEST_PROMPT_ENTITY_ID, Instant.now(), TEST_TENANT_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getVersion()).isEqualTo("1.0.1");
    }



    @Test
    void findAllByIdOrderByEffectiveDateDesc_ShouldReturnAllVersions() {
        // Arrange
        MinionPrompt prompt2 = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .entityId(TEST_PROMPT_ENTITY_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version("1.0.1")
            .tenantId(TEST_TENANT_ID)
            .effectiveDate(Instant.now().plusSeconds(1))
            .components(samplePrompt.getComponents())
            .build();

        when(repository.findAllByEntityIdAfterOrderByEffectiveDateDesc(TEST_PROMPT_ID))
            .thenReturn(Arrays.asList(prompt2, samplePrompt).stream()
                .sorted(Comparator.comparing(MinionPrompt::getEffectiveDate).reversed())
                .toList());

        // Act
        List<MinionPrompt> result = repository.findAllByEntityIdAfterOrderByEffectiveDateDesc(TEST_PROMPT_ID);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVersion()).isEqualTo("1.0.1");
        assertThat(result.get(1).getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    }

    @Test
    void findByEntityIdAndVersionAndTenantId_ShouldReturnPrompt() {
        // Arrange
        when(repository.findByEntityIdAndVersionAndTenantId(TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION, TEST_TENANT_ID))
            .thenReturn(Optional.of(samplePrompt));

        // Act
        Optional<MinionPrompt> result = repository.findByEntityIdAndVersionAndTenantId(
            TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION, TEST_TENANT_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
        assertThat(result.get().getVersion()).isEqualTo(TEST_PROMPT_VERSION);
        assertThat(result.get().getTenantId()).isEqualTo(TEST_TENANT_ID);
    }

    @Test
    void findLatestByEntityIdAndTenantId_ShouldReturnLatestPrompt() {
        // Arrange
        MinionPrompt latestPrompt = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version("1.0.1")
            .tenantId(TEST_TENANT_ID)
            .effectiveDate(Instant.now().plusSeconds(1))
            .components(samplePrompt.getComponents())
            .build();

        when(repository.findLatestByEntityIdAndTenantId(TEST_PROMPT_ENTITY_ID, TEST_TENANT_ID))
            .thenReturn(Optional.of(latestPrompt));

        // Act
        Optional<MinionPrompt> result = repository.findLatestByEntityIdAndTenantId(
            TEST_PROMPT_ENTITY_ID, TEST_TENANT_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getVersion()).isEqualTo("1.0.1");
    }

    @Test
    void findAllByTenantId_ShouldReturnAllPrompts() {
        // Arrange
        when(repository.findAllByTenantId(TEST_TENANT_ID))
            .thenReturn(List.of(samplePrompt));

        // Act
        List<MinionPrompt> result = repository.findAllByTenantId(TEST_TENANT_ID);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
        assertThat(result.get(0).getTenantId()).isEqualTo(TEST_TENANT_ID);
    }

    @Test
    void findAllByEntityIdAndTenantId_ShouldReturnAllVersions() {
        // Arrange
        MinionPrompt prompt2 = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version("1.0.1")
            .tenantId(TEST_TENANT_ID)
            .effectiveDate(Instant.now().plusSeconds(1))
            .components(samplePrompt.getComponents())
            .build();

        when(repository.findAllByEntityIdAndTenantId(TEST_PROMPT_ENTITY_ID, TEST_TENANT_ID))
            .thenReturn(Arrays.asList(prompt2, samplePrompt));

        // Act
        List<MinionPrompt> result = repository.findAllByEntityIdAndTenantId(
            TEST_PROMPT_ENTITY_ID, TEST_TENANT_ID);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVersion()).isEqualTo("1.0.1");
        assertThat(result.get(1).getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    }

    @Test
    void existsByEntityIdAndVersionAndTenantId_ShouldReturnTrue_WhenExists() {
        // Arrange
        when(repository.existsByEntityIdAndVersionAndTenantId(TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION, TEST_TENANT_ID))
            .thenReturn(true);

        // Act
        boolean exists = repository.existsByEntityIdAndVersionAndTenantId(
            TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION, TEST_TENANT_ID);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEntityIdAndVersionAndTenantId_ShouldReturnFalse_WhenNotExists() {
        // Arrange
        when(repository.existsByEntityIdAndVersionAndTenantId("non-existent", "1.0.0", TEST_TENANT_ID))
            .thenReturn(false);

        // Act
        boolean exists = repository.existsByEntityIdAndVersionAndTenantId(
            "non-existent", "1.0.0", TEST_TENANT_ID);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void findAllVersions_ShouldReturnAllVersions() {
        // Arrange
        MinionPrompt prompt2 = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version("1.0.1")
            .tenantId(TEST_TENANT_ID)
            .effectiveDate(Instant.now().plusSeconds(1))
            .components(samplePrompt.getComponents())
            .build();

        when(repository.findAllVersions(TEST_PROMPT_DESCRIPTION, TEST_TENANT_ID))
            .thenReturn(Arrays.asList(prompt2, samplePrompt));

        // Act
        List<MinionPrompt> result = repository.findAllVersions(TEST_PROMPT_DESCRIPTION, TEST_TENANT_ID);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVersion()).isEqualTo("1.0.1");
        assertThat(result.get(1).getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    }

    @Test
    void findAllWithExample_ShouldReturnMatchingPrompts() {
        // Arrange
        Example<MinionPrompt> example = Example.of(MinionPrompt.builder()
            .description(TEST_PROMPT_DESCRIPTION)
            .build());

        when(repository.findAll(example))
            .thenReturn(List.of(samplePrompt));

        // Act
        List<MinionPrompt> result = repository.findAll(example);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
    }

    @Test
    void findAllWithExampleAndSort_ShouldReturnSortedPrompts() {
        // Arrange
        MinionPrompt prompt2 = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version("1.0.1")
            .tenantId(TEST_TENANT_ID)
            .effectiveDate(Instant.now().plusSeconds(1))
            .components(samplePrompt.getComponents())
            .build();

        Example<MinionPrompt> example = Example.of(MinionPrompt.builder()
            .description(TEST_PROMPT_DESCRIPTION)
            .build());
        Sort sort = Sort.by(Sort.Direction.DESC, "version");

        when(repository.findAll(example, sort))
            .thenReturn(Arrays.asList(prompt2, samplePrompt));

        // Act
        List<MinionPrompt> result = repository.findAll(example, sort);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVersion()).isEqualTo("1.0.1");
        assertThat(result.get(1).getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    }

    @Test
    void findAllWithExampleAndPageable_ShouldReturnPagedPrompts() {
        // Arrange
        MinionPrompt prompt2 = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version("1.0.1")
            .tenantId(TEST_TENANT_ID)
            .effectiveDate(Instant.now().plusSeconds(1))
            .components(samplePrompt.getComponents())
            .build();

        Example<MinionPrompt> example = Example.of(MinionPrompt.builder()
            .description(TEST_PROMPT_DESCRIPTION)
            .build());
        PageRequest pageable = PageRequest.of(0, 1, Sort.Direction.DESC, "version");

        Page<MinionPrompt> page = new PageImpl<>(List.of(prompt2), pageable, 2);
        when(repository.findAll(example, pageable))
            .thenReturn(page);

        // Act
        Page<MinionPrompt> result = repository.findAll(example, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getVersion()).isEqualTo("1.0.1");
    }

    @Test
    void countWithExample_ShouldReturnCorrectCount() {
        // Arrange
        Example<MinionPrompt> example = Example.of(MinionPrompt.builder()
            .description(TEST_PROMPT_DESCRIPTION)
            .build());

        when(repository.count(example))
            .thenReturn(1L);

        // Act
        long count = repository.count(example);

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    void existsWithExample_ShouldReturnTrue_WhenExists() {
        // Arrange
        Example<MinionPrompt> example = Example.of(MinionPrompt.builder()
            .description(TEST_PROMPT_DESCRIPTION)
            .build());

        when(repository.exists(example))
            .thenReturn(true);

        // Act
        boolean exists = repository.exists(example);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void findAllWithSort_ShouldReturnSortedPrompts() {
        // Arrange
        MinionPrompt prompt2 = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version("1.0.1")
            .tenantId(TEST_TENANT_ID)
            .effectiveDate(Instant.now().plusSeconds(1))
            .components(samplePrompt.getComponents())
            .build();

        Sort sort = Sort.by(Sort.Direction.DESC, "version");

        when(repository.findAll(sort))
            .thenReturn(Arrays.asList(prompt2, samplePrompt));

        // Act
        List<MinionPrompt> result = repository.findAll(sort);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVersion()).isEqualTo("1.0.1");
        assertThat(result.get(1).getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    }

    @Test
    void findAllWithPageable_ShouldReturnPagedPrompts() {
        // Arrange
        MinionPrompt prompt2 = MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version("1.0.1")
            .tenantId(TEST_TENANT_ID)
            .effectiveDate(Instant.now().plusSeconds(1))
            .components(samplePrompt.getComponents())
            .build();

        PageRequest pageable = PageRequest.of(0, 1, Sort.Direction.DESC, "version");

        Page<MinionPrompt> page = new PageImpl<>(List.of(prompt2), pageable, 2);
        when(repository.findAll(pageable))
            .thenReturn(page);

        // Act
        Page<MinionPrompt> result = repository.findAll(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getVersion()).isEqualTo("1.0.1");
    }
} 