package com.minionsai.claude.prompt;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Document DB repository for storing and retrieving system prompts
 */
@Repository
public interface SystemPromptRepository extends MongoRepository<SystemPrompt, String> {

  /**
   * Find prompts by agent type
   */
  List<SystemPrompt> findByAgentType(String agentType);

  /**
   * Find the latest version of a prompt for an agent type
   */
  @Query(value = "{ 'agentType': ?0 }", sort = "{ 'updatedAt': -1 }")
  Optional<SystemPrompt> findLatestByAgentType(String agentType);

  /**
   * Find a specific version of a prompt
   */
  Optional<SystemPrompt> findByAgentTypeAndVersion(String agentType, String version);
}
