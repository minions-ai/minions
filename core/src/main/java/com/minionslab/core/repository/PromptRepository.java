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

  Optional<MinionPrompt> findActiveVersion(String entityId, Instant pointInTime);

  List<MinionPrompt> findAllByEntityIdAfterOrderByEffectiveDateDesc(String promptId);

  Optional<MinionPrompt> findByEntityIdAndVersion(String entityId, String version);

  Optional<MinionPrompt> findLatestByEntityId(String entityId);


  List<MinionPrompt> findAllByEntityId(String entityId);

  boolean existsByEntityIdAndVersion(String entityId, String version);

  List<MinionPrompt> findAllVersions(String entityId);

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