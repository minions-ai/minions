package com.minionslab.core.repository;

import com.minionslab.core.domain.MinionPrompt;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PromptRepository extends MongoRepository<MinionPrompt, String> {
    
    @Query("{ 'entityId': ?0, 'effectiveDate': { $lte: ?1 }, $or: [ { 'expiryDate': null }, { 'expiryDate': { $gt: ?1 } } ] , 'tenant_id': ?2}")
    Optional<MinionPrompt> findActiveVersion(String entityId, Instant pointInTime, String tenantId);
    
    List<MinionPrompt> findAllByEntityIdAfterOrderByEffectiveDateDesc(String promptId);
    
    Optional<MinionPrompt> findByEntityIdAndVersionAndTenantId(String entityId, String version, String tenantId);
    
    @Query("{ 'entityId': ?0, 'tenant_id': ?1 }")
    Optional<MinionPrompt> findLatestByEntityIdAndTenantId(String name, String tenantId);
    
    List<MinionPrompt> findAllByTenantId(String tenantId);
    
    List<MinionPrompt> findAllByEntityIdAndTenantId(String entityId, String tenantId);
    
    boolean existsByEntityIdAndVersionAndTenantId(String entityId, String version, String tenantId);
    
    @Query("{ 'entityId': ?0 , 'tenant_id': ?1}")
    List<MinionPrompt> findAllVersions(String entityId,String tenantId);



    // Default MongoRepository methods that need to be implemented
    @Override
    <S extends MinionPrompt> S insert(S entity);

    @Override
    <S extends MinionPrompt> List<S> insert(Iterable<S> entities);

    @Override
    <S extends MinionPrompt> List<S> findAll(Example<S> example);

    @Override
    <S extends MinionPrompt> List<S> findAll(Example<S> example, Sort sort);

    @Override
    <S extends MinionPrompt> Page<S> findAll(Example<S> example, Pageable pageable);

    @Override
    <S extends MinionPrompt> long count(Example<S> example);

    @Override
    <S extends MinionPrompt> boolean exists(Example<S> example);

    @Override
    List<MinionPrompt> findAll(Sort sort);

    @Override
    Page<MinionPrompt> findAll(Pageable pageable);

}