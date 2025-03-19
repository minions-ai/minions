package com.minionslab.core.repository;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import java.util.List;
import java.util.Optional;

public interface PromptRepository {

  Optional<MinionPrompt> findById(String id);

  Optional<MinionPrompt> findByTypeAndNameAndVersionAndTenantId(MinionType type, String name, String version, String tenantId);

  Optional<MinionPrompt> findLatestByTypeAndNameAndTenantId(MinionType type, String name, String tenantId);

  List<MinionPrompt> findAllByTenantId(String tenantId);

  List<MinionPrompt> findAllByTypeAndTenantId(MinionType type, String tenantId);

  boolean existsByTypeAndNameAndVersionAndTenantId(MinionType type, String name, String version, String tenantId);

  MinionPrompt save(MinionPrompt prompt);

  void deleteById(String id);

  Optional<MinionPrompt> findByNameAndVersionAndTenantId(String promptName, String promptVersion, String tenantId);
}