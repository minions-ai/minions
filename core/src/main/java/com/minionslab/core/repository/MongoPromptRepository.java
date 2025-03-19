package com.minionslab.core.repository;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "minions.prompt.storage", havingValue = "mongodb", matchIfMissing = true)
public class MongoPromptRepository implements PromptRepository {

  private final MongoTemplate mongoTemplate;

  public MongoPromptRepository(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override public Optional<MinionPrompt> findById(String id) {
    Query query = Query.query(Criteria.where("id").is(id));
    return Optional.ofNullable(mongoTemplate.findOne(query, MinionPrompt.class));
  }

  @Override
  public Optional<MinionPrompt> findByTypeAndNameAndVersionAndTenantId(
      MinionType type,
      String name,
      String version,
      String tenantId
  ) {
    Query query = new Query(Criteria.where("type").is(type)
        .and("name").is(name)
        .and("version").is(version)
        .and("tenantId").is(tenantId));

    return Optional.ofNullable(mongoTemplate.findOne(query, MinionPrompt.class));
  }

  @Override
  public Optional<MinionPrompt> findLatestByTypeAndNameAndTenantId(
      MinionType type,
      String name,
      String tenantId
  ) {
    Query query = new Query(Criteria.where("type").is(type)
        .and("name").is(name)
        .and("tenantId").is(tenantId))
        .with(Sort.by(Sort.Direction.DESC, "version"))
        .limit(1);

    return Optional.ofNullable(mongoTemplate.findOne(query, MinionPrompt.class));
  }

  @Override
  public List<MinionPrompt> findAllByTenantId(String tenantId) {
    Query query = new Query(Criteria.where("tenantId").is(tenantId));
    return mongoTemplate.find(query, MinionPrompt.class);
  }

  @Override
  public List<MinionPrompt> findAllByTypeAndTenantId(
      MinionType type,
      String tenantId
  ) {
    Query query = new Query(Criteria.where("type").is(type)
        .and("tenantId").is(tenantId));
    return mongoTemplate.find(query, MinionPrompt.class);
  }

  @Override
  public boolean existsByTypeAndNameAndVersionAndTenantId(
      MinionType type,
      String name,
      String version,
      String tenantId
  ) {
    Query query = new Query(Criteria.where("type").is(type)
        .and("name").is(name)
        .and("version").is(version)
        .and("tenantId").is(tenantId));

    return mongoTemplate.exists(query, MinionPrompt.class);
  }

  public List<MinionPrompt> findPromptsByAgentTypeAndNameAndTenantId(
      MinionType type,
      String name,
      String tenantId
  ) {
    Query query = new Query(Criteria.where("type").is(type)
        .and("name").is(name)
        .and("tenantId").is(tenantId));

    return mongoTemplate.find(query, MinionPrompt.class);
  }

  public Optional<MinionPrompt> findLatestPromptByAgentTypeAndNameAndTenantId(
      MinionType type,
      String name,
      String tenantId
  ) {
    Query query = new Query(Criteria.where("type").is(type)
        .and("name").is(name)
        .and("tenantId").is(tenantId))
        .with(Sort.by(Sort.Direction.DESC, "version"))
        .limit(1);

    return Optional.ofNullable(mongoTemplate.findOne(query, MinionPrompt.class));
  }

  // Additional methods from MongoRepository that might be needed
  public MinionPrompt save(MinionPrompt prompt) {
    return mongoTemplate.save(prompt);
  }

  public void deleteById(String id) {
    Query query = new Query(Criteria.where("id").is(id));
    mongoTemplate.remove(query, MinionPrompt.class);
  }

  @Override public Optional<MinionPrompt> findByNameAndVersionAndTenantId(String promptName, String promptVersion, String tenantId) {
    Query query = new Query(Criteria.where("name").is(promptName)
        .and("version").is(promptVersion)
        .and("tenantId").is(tenantId));
    return Optional.ofNullable(mongoTemplate.findOne(query, MinionPrompt.class));
  }
} 