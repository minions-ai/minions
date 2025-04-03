package com.minionslab.core.repository;

import static com.minionslab.core.test.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_ENTITY_ID;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_ID;

import static com.minionslab.core.test.TestConstants.TEST_PROMPT_TYPE;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_VERSION;
import static com.minionslab.core.test.TestConstants.TEST_TENANT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class MongoPromptRepositoryTest extends BaseMongoTest {



    @Autowired
    private PromptRepository repository;
    private MinionPrompt samplePrompt;

    @BeforeEach
    void setUp() {
        samplePrompt = MinionPrompt.builder()
            .id("prompt123")
            .description(TEST_PROMPT_DESCRIPTION)
            .version(TEST_PROMPT_VERSION)
            .tenantId(TEST_TENANT_ID).build();
    }

    @Test
    void findLatestByTypeAndNameAndTenantId_ShouldReturnLatestPrompt() {
        when(mongoTemplate.findOne(any(Query.class), eq(MinionPrompt.class)))
            .thenReturn(samplePrompt);

        Optional<MinionPrompt> result = repository.findLatestByEntityIdAndTenantId(
            TEST_PROMPT_ENTITY_ID, TEST_TENANT_ID
        );

        assertThat(result).isPresent();
        verify(mongoTemplate).findOne(
            argThat(query ->
                query.getSortObject().containsKey("effectiveDate") &&
                    query.getLimit() == 1 &&
                    query.getQueryObject().get("type").equals(TEST_PROMPT_TYPE) &&
                    query.getQueryObject().get("description").equals(TEST_PROMPT_DESCRIPTION) &&
                    query.getQueryObject().get("tenantId").equals(TEST_TENANT_ID)
            ),
            eq(MinionPrompt.class)
        );
    }

    @Test
    void findAllByTenantId_ShouldReturnAllPrompts() {
        List<MinionPrompt> prompts = Arrays.asList(samplePrompt, samplePrompt);
        when(mongoTemplate.find(any(Query.class), eq(MinionPrompt.class)))
            .thenReturn(prompts);

        List<MinionPrompt> result = repository.findAllByTenantId(TEST_TENANT_ID);

        assertThat(result).hasSize(2);
        verify(mongoTemplate).find(any(Query.class), eq(MinionPrompt.class));
    }

    @Test
    void existsByTypeAndNameAndVersionAndTenantId_ShouldReturnTrue_WhenExists() {
        when(mongoTemplate.exists(any(Query.class), eq(MinionPrompt.class)))
            .thenReturn(true);

        boolean exists = repository.existsByEntityIdAndVersionAndTenantId(
            TEST_PROMPT_ENTITY_ID, TEST_PROMPT_VERSION, TEST_TENANT_ID
        );

        assertThat(exists).isTrue();
    }

    @Test
    void findByNameAndVersionAndTenantId_ShouldReturnPrompt_WhenExists() {
        when(mongoTemplate.findOne(any(Query.class), eq(MinionPrompt.class)))
            .thenReturn(samplePrompt);

        Optional<MinionPrompt> result = repository.findByEntityIdAndVersionAndTenantId(
            TEST_PROMPT_ID, TEST_PROMPT_VERSION, TEST_TENANT_ID
        );

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
        assertThat(result.get().getVersion()).isEqualTo(TEST_PROMPT_VERSION);
        assertThat(result.get().getTenantId()).isEqualTo(TEST_TENANT_ID);
    }

    @Test
    void findActiveVersion_ShouldReturnActivePrompt() {
        // Arrange
        Instant now = Instant.now();
        samplePrompt = createSamplePrompt(now);
        repository.save(samplePrompt);

        // Act
        Optional<MinionPrompt> result = repository.findActiveVersion(TEST_PROMPT_ENTITY_ID, now, TEST_TENANT_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
    }



    @Test
    void findActiveVersion_ShouldNotReturnExpiredPrompt() {
        // Arrange
        Instant now = Instant.now();
        samplePrompt = createSamplePrompt(now);
        samplePrompt.setExpiryDate(now.minusSeconds(1));
        repository.save(samplePrompt);

        // Act
        Optional<MinionPrompt> result = repository.findActiveVersion(TEST_PROMPT_ENTITY_ID, now, TEST_TENANT_ID);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByIdOrderByEffectiveDateDesc_ShouldReturnAllVersions() {
        // Arrange
        Instant now = Instant.now();
        MinionPrompt prompt1 = createSamplePrompt(now);
        MinionPrompt prompt2 = createSamplePrompt(now.plusSeconds(1));
        prompt2.setVersion("1.0.1");
        repository.saveAll(Arrays.asList(prompt1, prompt2));

        // Act
        List<MinionPrompt> result = repository.findAllByEntityIdAfterOrderByEffectiveDateDesc(TEST_PROMPT_ID);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVersion()).isEqualTo("1.0.1");
        assertThat(result.get(1).getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    }

    @Test
    void findAllByTypeAndNameAndTenantId_ShouldReturnAllVersions() {
        List<MinionPrompt> prompts = Arrays.asList(samplePrompt, samplePrompt);
        when(mongoTemplate.find(any(Query.class), eq(MinionPrompt.class)))
            .thenReturn(prompts);

        List<MinionPrompt> result = repository.findAllByEntityIdAndTenantId(
            TEST_PROMPT_ENTITY_ID, TEST_TENANT_ID
        );

        assertThat(result).hasSize(2);
        verify(mongoTemplate).find(
            argThat(query ->
                query.getQueryObject().get("type").equals(TEST_PROMPT_TYPE) &&
                    query.getQueryObject().get("description").equals(TEST_PROMPT_DESCRIPTION) &&
                    query.getQueryObject().get("tenantId").equals(TEST_TENANT_ID)
            ),
            eq(MinionPrompt.class)
        );
    }

    @Test
    void findAllVersions_ShouldReturnAllVersions() {
        // Arrange
        Instant now = Instant.now();
        MinionPrompt prompt1 = createSamplePrompt(now);
        MinionPrompt prompt2 = createSamplePrompt(now.plusSeconds(1));
        prompt2.setVersion("1.0.1");
        repository.saveAll(Arrays.asList(prompt1, prompt2));

        // Act
        List<MinionPrompt> result = repository.findAllVersions(TEST_PROMPT_ENTITY_ID, TEST_TENANT_ID);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVersion()).isEqualTo("1.0.1");
        assertThat(result.get(1).getVersion()).isEqualTo(TEST_PROMPT_VERSION);
    }

    @Test
    void findAllWithExample_ShouldReturnMatchingPrompts() {
        // Arrange
        samplePrompt = createSamplePrompt(Instant.now());
        repository.save(samplePrompt);

        Example<MinionPrompt> example = Example.of(MinionPrompt.builder()
            .description(TEST_PROMPT_DESCRIPTION)
            .build());

        // Act
        List<MinionPrompt> result = repository.findAll(example);

        // Assert
        assertThat(result).hasSize(1);
    assertThat(result.get(0).getDescription()).isEqualTo(TEST_PROMPT_DESCRIPTION);
    }

    @Test
    void findAllWithExampleAndSort_ShouldReturnSortedPrompts() {
        // Arrange
        Instant now = Instant.now();
        MinionPrompt prompt1 = createSamplePrompt(now);
        MinionPrompt prompt2 = createSamplePrompt(now.plusSeconds(1));
        prompt2.setVersion("1.0.1");
        repository.saveAll(Arrays.asList(prompt1, prompt2));

        Example<MinionPrompt> example = Example.of(MinionPrompt.builder()
            .description(TEST_PROMPT_DESCRIPTION)
            .build());
        Sort sort = Sort.by(Sort.Direction.DESC, "version");

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
        Instant now = Instant.now();
        MinionPrompt prompt1 = createSamplePrompt(now);
        MinionPrompt prompt2 = createSamplePrompt(now.plusSeconds(1));
        prompt2.setVersion("1.0.1");
        repository.saveAll(Arrays.asList(prompt1, prompt2));

        Example<MinionPrompt> example = Example.of(MinionPrompt.builder()
            .description(TEST_PROMPT_DESCRIPTION)
            .build());
        PageRequest pageable = PageRequest.of(0, 1, Sort.Direction.DESC, "version");

        // Act
        var result = repository.findAll(example, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getVersion()).isEqualTo("1.0.1");
    }

    @Test
    void countWithExample_ShouldReturnCorrectCount() {
        // Arrange
        samplePrompt = createSamplePrompt(Instant.now());
        repository.save(samplePrompt);

        Example<MinionPrompt> example = Example.of(MinionPrompt.builder()
            .description(TEST_PROMPT_DESCRIPTION)
            .build());

        // Act
        long count = repository.count(example);

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    void existsWithExample_ShouldReturnTrue_WhenExists() {
        // Arrange
        samplePrompt = createSamplePrompt(Instant.now());
        repository.save(samplePrompt);

        Example<MinionPrompt> example = Example.of(MinionPrompt.builder()
            .description(TEST_PROMPT_DESCRIPTION)
            .build());

        // Act
        boolean exists = repository.exists(example);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void findAllWithSort_ShouldReturnSortedPrompts() {
        // Arrange
        Instant now = Instant.now();
        MinionPrompt prompt1 = createSamplePrompt(now);
        MinionPrompt prompt2 = createSamplePrompt(now.plusSeconds(1));
        prompt2.setVersion("1.0.1");
        repository.saveAll(Arrays.asList(prompt1, prompt2));

        Sort sort = Sort.by(Sort.Direction.DESC, "version");

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
        Instant now = Instant.now();
        MinionPrompt prompt1 = createSamplePrompt(now);
        MinionPrompt prompt2 = createSamplePrompt(now.plusSeconds(1));
        prompt2.setVersion("1.0.1");
        repository.saveAll(Arrays.asList(prompt1, prompt2));

        PageRequest pageable = PageRequest.of(0, 1, Sort.Direction.DESC, "version");

        // Act
        var result = repository.findAll(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getVersion()).isEqualTo("1.0.1");
    }

    private MinionPrompt createSamplePrompt(Instant effectiveDate) {
        return MinionPrompt.builder()
            .id(TEST_PROMPT_ID)
            .description(TEST_PROMPT_DESCRIPTION)
            .version(TEST_PROMPT_VERSION)
            .tenantId(TEST_TENANT_ID)
            .effectiveDate(effectiveDate)
            .components(Map.of(PromptType.SYSTEM, PromptComponent.builder()
                .type(PromptType.SYSTEM)
                .text("Test system prompt")
                .build()))
            .build();
    }

    // Custom ArgumentMatcher for Query objects
    private static class QueryMatcher implements org.mockito.ArgumentMatcher<Query> {

        private final Criteria expectedCriteria;

        QueryMatcher(Criteria expectedCriteria) {
            this.expectedCriteria = expectedCriteria;
        }

        @Override
        public boolean matches(Query query) {
            return query.getQueryObject().equals(expectedCriteria.getCriteriaObject());
        }
    }
} 