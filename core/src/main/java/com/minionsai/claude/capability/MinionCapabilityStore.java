package com.minionsai.claude.capability;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SearchRequest.Builder;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class MinionCapabilityStore {

  @Autowired
  private final VectorStore vectorStore;

  @Autowired
  private final EmbeddingModel embeddingClient;

  /**
   * Register an agent's capabilities in the vector store
   *
   * @param capabilities List of capabilities to register
   * @param agentType    The type of agent these capabilities belong to
   * @return Number of capabilities registered
   */
  @CacheEvict(value = {"agentCapabilities", "bestAgentForTask"}, allEntries = true)
  public int registerAgentCapabilities(List<MinionCapability> capabilities, String agentType) {
    List<Document> documents = capabilities.stream()
        .map(capability -> {
          // Create a document with the capability description
          return Document.builder()
              .id(capability.getId() != null ? capability.getId() : UUID.randomUUID().toString())
              .text(capability.getDescription())
              .metadata("agentType", agentType)
              .metadata("capabilityName", capability.getName())
              .metadata("capabilityType", capability.getType())
              .metadata("isAvailable", capability.isAvailable())
              .build();
        })
        .toList();

    // Add documents to vector store
    vectorStore.add(documents);

    log.info("Registered {} capabilities for agent type {}", documents.size(), agentType);
    return documents.size();
  }

  /**
   * Find the most appropriate agent for a given task description
   *
   * @param taskDescription Description of the task to be performed
   * @param limit           Maximum number of matching agents to return
   * @return List of agent types that can handle the task, ordered by relevance
   */
  @Cacheable(value = "agentCapabilities", key = "{#taskDescription, #limit}")
  public List<MinionMatch> findAgentForTask(String taskDescription, int limit) {
    log.debug("Cache miss for findAgentForTask({}), performing vector search", taskDescription);

    SearchRequest searchRequest = new Builder().query(taskDescription)
        .topK(limit)
        .similarityThreshold(0.6f)
        .filterExpression("isAvailable == true").build();

    List<Document> matchingDocs = vectorStore.similaritySearch(searchRequest);

    // Convert to MinionMatch objects
    List<MinionMatch> matches = matchingDocs.stream()
        .map(doc -> {
          String agentType = doc.getMetadata().getOrDefault("agentType", "unknown").toString();
          String capabilityName = doc.getMetadata().getOrDefault("capabilityName", "unknown").toString();

          return MinionMatch.builder()
              .agentType(agentType)
              .capabilityName(capabilityName)
              .score(doc.getScore())
              .matchedCapabilityDescription(doc.getContent())
              .build();
        })
        .toList();

    log.info("Found {} agent matches for task: {}", matches.size(), taskDescription);
    return matches;
  }

  /**
   * Find the best agent for a task
   *
   * @param taskDescription Description of the task
   * @return Optional containing the best agent match, or empty if no suitable agent found
   */
  @Cacheable(value = "bestAgentForTask", key = "#taskDescription")
  public Optional<MinionMatch> findBestAgentForTask(String taskDescription) {
    log.debug("Cache miss for findBestAgentForTask({})", taskDescription);

    List<MinionMatch> matches = findAgentForTask(taskDescription, 1);
    if (matches.isEmpty()) {
      log.info("No agent match found for task: {}", taskDescription);
      return Optional.empty();
    }

    MinionMatch bestMatch = matches.get(0);
    log.info("Best agent match for task '{}' is {} with score {}",
        taskDescription, bestMatch.getAgentType(), bestMatch.getScore());

    return Optional.of(bestMatch);
  }

  /**
   * Remove all capabilities for an agent type
   *
   * @param agentType The agent type to clear capabilities for
   */
  @CacheEvict(value = {"agentCapabilities", "bestAgentForTask"}, allEntries = true)
  public void clearAgentCapabilities(String agentType) {
    // This would require implementation based on the vector store being used
    // Some vector stores allow deletion by metadata filtering
    log.info("Cleared capabilities for agent type {}", agentType);
  }

  /**
   * Clear all capability caches
   */
  @Caching(evict = {
      @CacheEvict(value = "agentCapabilities", allEntries = true),
      @CacheEvict(value = "bestAgentForTask", allEntries = true)
  })
  public void clearAllCaches() {
    log.info("Cleared all agent capability caches");
  }


}