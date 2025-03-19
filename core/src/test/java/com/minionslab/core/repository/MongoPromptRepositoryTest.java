package com.minionslab.core.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class MongoPromptRepositoryTest {

  private static final String TENANT_ID = "tenant123";
  private static final String PROMPT_NAME = "testPrompt";
  private static final String PROMPT_VERSION = "1.0";
  private static final MinionType PROMPT_TYPE = MinionType.USER_DEFINED_AGENT;
  @Mock
  private MongoTemplate mongoTemplate;
  private MongoPromptRepository repository;
  private MinionPrompt samplePrompt;

  @BeforeEach
  void setUp() {
    repository = new MongoPromptRepository(mongoTemplate);

    samplePrompt = MinionPrompt.builder()
        .id("prompt123")
        .name(PROMPT_NAME)
        .version(PROMPT_VERSION)
        .type(PROMPT_TYPE)
        .tenantId(TENANT_ID).build();
  }

  @Test
  void findById_ShouldReturnPrompt_WhenExists() {
    when(mongoTemplate.findOne(any(Query.class), eq(MinionPrompt.class)))
        .thenReturn(samplePrompt);

    Optional<MinionPrompt> result = repository.findById("prompt123");

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo("prompt123");
    verify(mongoTemplate).findOne(any(Query.class), eq(MinionPrompt.class));
  }

  @Test
  void findByTypeAndNameAndVersionAndTenantId_ShouldReturnPrompt_WhenExists() {
    when(mongoTemplate.findOne(any(Query.class), eq(MinionPrompt.class)))
        .thenReturn(samplePrompt);

    Optional<MinionPrompt> result = repository.findByTypeAndNameAndVersionAndTenantId(
        PROMPT_TYPE, PROMPT_NAME, PROMPT_VERSION, TENANT_ID
    );

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo(PROMPT_NAME);
    assertThat(result.get().getVersion()).isEqualTo(PROMPT_VERSION);
    assertThat(result.get().getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void findLatestByTypeAndNameAndTenantId_ShouldReturnLatestPrompt() {
    when(mongoTemplate.findOne(any(Query.class), eq(MinionPrompt.class)))
        .thenReturn(samplePrompt);

    Optional<MinionPrompt> result = repository.findLatestByTypeAndNameAndTenantId(
        PROMPT_TYPE, PROMPT_NAME, TENANT_ID
    );

    assertThat(result).isPresent();
    verify(mongoTemplate).findOne(
        argThat(query ->
            query.getSortObject().containsKey("version") &&
                query.getLimit() == 1
        ),
        eq(MinionPrompt.class)
    );
  }

  @Test
  void findAllByTenantId_ShouldReturnAllPrompts() {
    List<MinionPrompt> prompts = Arrays.asList(samplePrompt, samplePrompt);
    when(mongoTemplate.find(any(Query.class), eq(MinionPrompt.class)))
        .thenReturn(prompts);

    List<MinionPrompt> result = repository.findAllByTenantId(TENANT_ID);

    assertThat(result).hasSize(2);
    verify(mongoTemplate).find(any(Query.class), eq(MinionPrompt.class));
  }

  @Test
  void existsByTypeAndNameAndVersionAndTenantId_ShouldReturnTrue_WhenExists() {
    when(mongoTemplate.exists(any(Query.class), eq(MinionPrompt.class)))
        .thenReturn(true);

    boolean exists = repository.existsByTypeAndNameAndVersionAndTenantId(
        PROMPT_TYPE, PROMPT_NAME, PROMPT_VERSION, TENANT_ID
    );

    assertThat(exists).isTrue();
  }

  @Test
  void save_ShouldReturnSavedPrompt() {
    when(mongoTemplate.save(any(MinionPrompt.class)))
        .thenReturn(samplePrompt);

    MinionPrompt result = repository.save(samplePrompt);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(samplePrompt.getId());
    verify(mongoTemplate).save(samplePrompt);
  }

  @Test
  void deleteById_ShouldCallMongoTemplate() {
    repository.deleteById("prompt123");

    verify(mongoTemplate).remove(
        argThat(query ->
            query.getQueryObject().get("id").equals("prompt123")
        ),
        eq(MinionPrompt.class)
    );
  }

  @Test
  void findByNameAndVersionAndTenantId_ShouldReturnPrompt_WhenExists() {
    when(mongoTemplate.findOne(any(Query.class), eq(MinionPrompt.class)))
        .thenReturn(samplePrompt);

    Optional<MinionPrompt> result = repository.findByNameAndVersionAndTenantId(
        PROMPT_NAME, PROMPT_VERSION, TENANT_ID
    );

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo(PROMPT_NAME);
    assertThat(result.get().getVersion()).isEqualTo(PROMPT_VERSION);
    assertThat(result.get().getTenantId()).isEqualTo(TENANT_ID);
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