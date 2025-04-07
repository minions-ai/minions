package com.minionslab.core.repository;

import com.minionslab.core.common.exception.MissingContextException;
import com.minionslab.core.common.exception.PromptException.InvalidPromptIdException;
import com.minionslab.core.context.MinionContext;
import com.minionslab.core.context.MinionContextHolder;
import com.minionslab.core.domain.MinionPrompt;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Concrete implementation of PromptRepository that automatically handles tenant IDs. This implementation ensures that all operations are
 * scoped to the current tenant.
 */
@Repository
public class PromptRepositoryImpl extends SimpleMongoRepository<MinionPrompt, String> implements PromptRepository {

  private final MongoTemplate mongoTemplate;


  public PromptRepositoryImpl(MongoTemplate mongoTemplate) {
    super(new MongoRepositoryFactory(mongoTemplate).getEntityInformation(MinionPrompt.class), mongoTemplate);
    this.mongoTemplate = mongoTemplate;
  }

  /**
   * Gets the current tenant ID from the context.
   *
   * @return the current tenant ID
   * @throws MissingContextException if no context or tenant ID is found
   */
  private String getCurrentTenantId() {
    MinionContext context = MinionContextHolder.getRequiredContext();
    if (context == null || context.getTenantId() == null) {
      throw new MissingContextException("Tenant ID is required but not found in the current context");
    }
    return context.getTenantId();
  }

  /**
   * Creates a new query with tenant criteria.
   *
   * @return a new query with tenant criteria
   */
  private Query getTenantQuery() {
    String tenantId = getCurrentTenantId();
    return new Query(Criteria.where("tenant_id").is(tenantId));
  }

  /**
   * Adds tenant ID criteria to a query.
   *
   * @param query the query to add tenant ID criteria to
   * @return the modified query
   */
  private Query addTenantCriteria(Query query) {
    String tenantId = getCurrentTenantId();
    query.addCriteria(Criteria.where("tenant_id").is(tenantId));
    return query;
  }
  @Override
  public Optional<MinionPrompt> findActiveVersion(String entityId, Instant pointInTime) {
    Query query = getTenantQuery();
    query.addCriteria(Criteria.where("entityId").is(entityId)
        .and("effectiveDate").lte(pointInTime)
        .orOperator(
            Criteria.where("expiryDate").is(null),
            Criteria.where("expiryDate").gt(pointInTime)
        ));

    // Add sorting by effectiveDate descending to get the most recent active version
    query.with(Sort.by(Sort.Direction.DESC, "effectiveDate"));

    // Limit to 1 result since we only need the most recent active version
    query.limit(1);

    // Use findOne instead of find to get a single result
    MinionPrompt result = mongoTemplate.findOne(query, MinionPrompt.class);

    return Optional.ofNullable(result);
  }


  @Override
  public List<MinionPrompt> findAllByEntityIdAfterOrderByEffectiveDateDesc(String promptEntityId) {
    Query query = getTenantQuery();
    query.addCriteria(Criteria.where("entityId").is(promptEntityId));
    query.with(Sort.by(Sort.Direction.DESC, "effectiveDate"));
    return mongoTemplate.find(query, MinionPrompt.class);
  }

  @Override
  public Optional<MinionPrompt> findByEntityIdAndVersion(String entityId, String version) {
    Query query = getTenantQuery();
    query.addCriteria(Criteria.where("entityId").is(entityId)
        .and("version").is(version));

    return Optional.ofNullable(mongoTemplate.findOne(query, MinionPrompt.class));
  }

  @Override
  public Optional<MinionPrompt> findLatestByEntityId(String entityId) {
    Query query = getTenantQuery();
    query.addCriteria(Criteria.where("entityId").is(entityId));
    query.with(Sort.by(Sort.Direction.DESC, "effectiveDate"));
    query.limit(1);

    return Optional.ofNullable(mongoTemplate.findOne(query, MinionPrompt.class));
  }


  @Override
  public List<MinionPrompt> findAllByEntityId(String entityId) {
    Query query = getTenantQuery();
    query.addCriteria(Criteria.where("entityId").is(entityId));
    return mongoTemplate.find(query, MinionPrompt.class);
  }

  @Override
  public boolean existsByEntityIdAndVersion(String entityId, String version) {
    Query query = getTenantQuery();
    query.addCriteria(Criteria.where("entityId").is(entityId)
        .and("version").is(version));

    return mongoTemplate.exists(query, MinionPrompt.class);
  }

  @Override
  public List<MinionPrompt> findAllVersions(String entityId) {
    Query query = getTenantQuery();
    query.addCriteria(Criteria.where("entityId").is(entityId));
    query.with(Sort.by(Sort.Direction.DESC, "effectiveDate"));

    return mongoTemplate.find(query, MinionPrompt.class);
  }

  @Override
  public Optional<MinionPrompt> findById(String id) {
    Query query = getTenantQuery();
    query.addCriteria(Criteria.where("_id").is(id));

    return Optional.ofNullable(mongoTemplate.findOne(query, MinionPrompt.class));
  }

  @Override
  public void deleteById(String id) {
    Query query = getTenantQuery();
    query.addCriteria(Criteria.where("_id").is(id));

    mongoTemplate.remove(query, MinionPrompt.class);
  }

  @Override
  public <S extends MinionPrompt> List<S> findAll(Example<S> example) {
    Query query = getTenantQuery();
    // Add criteria from the example
    query.addCriteria(createCriteriaFromExample(example));
    return mongoTemplate.find(query, example.getProbeType());
  }

  @Override
  public <S extends MinionPrompt> List<S> findAll(Example<S> example, Sort sort) {
    Query query = getTenantQuery();
    // Add criteria from the example
    query.addCriteria(createCriteriaFromExample(example));
    query.with(sort);
    return mongoTemplate.find(query, example.getProbeType());
  }

  @Override
  public <S extends MinionPrompt> Page<S> findAll(Example<S> example, Pageable pageable) {
    Query query = getTenantQuery();
    // Add criteria from the example
    query.addCriteria(createCriteriaFromExample(example));
    query.with(pageable);

    long total = mongoTemplate.count(query, example.getProbeType());
    List<S> content = mongoTemplate.find(query, example.getProbeType());

    return new org.springframework.data.domain.PageImpl<>(content, pageable, total);
  }

  @Override
  public <S extends MinionPrompt> long count(Example<S> example) {
    Query query = getTenantQuery();
    // Add criteria from the example
    query.addCriteria(createCriteriaFromExample(example));
    return mongoTemplate.count(query, example.getProbeType());
  }

  @Override
  public <S extends MinionPrompt> boolean exists(Example<S> example) {
    Query query = getTenantQuery();
    // Add criteria from the example
    query.addCriteria(createCriteriaFromExample(example));
    return mongoTemplate.exists(query, example.getProbeType());
  }

  /**
   * Creates a Criteria object from an Example.
   *
   * @param example the example to create criteria from
   * @return the created criteria
   */
  private <S extends MinionPrompt> Criteria createCriteriaFromExample(Example<S> example) {
    // This is a simplified implementation
    // In a real implementation, you would need to convert the example to criteria
    // For now, we'll just return a criteria that matches the ID if it exists
    S probe = example.getProbe();
    if (probe.getId() != null) {
      return Criteria.where("_id").is(probe.getId());
    }
    return new Criteria();
  }

  @Override
  public List<MinionPrompt> findAll(Sort sort) {
    Query query = getTenantQuery();
    query.with(sort);
    return mongoTemplate.find(query, MinionPrompt.class);
  }

  @Override
  public Page<MinionPrompt> findAll(Pageable pageable) {
    Query query = getTenantQuery();

    // Get total count before applying pagination
    long total = mongoTemplate.count(query, MinionPrompt.class);

    // Apply pagination
    query.with(pageable);

    // Get the content for the current page
    List<MinionPrompt> content = mongoTemplate.find(query, MinionPrompt.class);

    // Create and return a PageImpl with the correct total count
    return new org.springframework.data.domain.PageImpl<>(content, pageable, total);
  }

  @Override
  public List<MinionPrompt> findAll() {
    return mongoTemplate.find(getTenantQuery(), MinionPrompt.class);
  }

  @Override
  public <S extends MinionPrompt> S save(S entity) {
    // Ensure tenant ID is set
    if (entity.getTenantId() == null) {
      entity.setTenantId(getCurrentTenantId());
    }
    return super.save(entity);
  }

  @Override
  public <S extends MinionPrompt> List<S> saveAll(Iterable<S> entities) {
    // Ensure tenant ID is set for all entities
    String tenantId = getCurrentTenantId();
    entities.forEach(entity -> {
      if (entity.getTenantId() == null) {
        entity.setTenantId(tenantId);
      }
    });
    return super.saveAll(entities);
  }
} 