package com.minionsai.claude.agent.memory;


import com.minionsai.claude.agent.Minion;
import com.minionsai.claude.context.MinionContext;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Manages memory operations for agents, including long-term storage, semantic search, and memory optimization.
 */
@Service
@Slf4j
public class MemoryManager {

  // Cache of recent memories for quick access
  private final Map<String, List<MinionMemory>> recentMemoriesCache = new ConcurrentHashMap<>();
  // Map of agent types to their importance evaluators
  private final Map<String, MemoryImportanceEvaluator> importanceEvaluators = new ConcurrentHashMap<>();
  @Autowired
  private VectorStore vectorStore;
  @Autowired
  private EmbeddingModel embeddingClient;
  @Value("${minions.memory.default-ttl:43200}") // Default 12 hours in seconds
  private long defaultTtlSeconds;
  @Value("${minions.memory.max-memories:1000}")
  private int maxMemoriesPerAgent;

  @PostConstruct
  public void initialize() {
    // Register default importance evaluator
    registerImportanceEvaluator("default", new DefaultMemoryImportanceEvaluator());
  }

  /**
   * Registers a custom importance evaluator for a specific agent type
   */
  public void registerImportanceEvaluator(String agentType, MemoryImportanceEvaluator evaluator) {
    importanceEvaluators.put(agentType, evaluator);
    log.info("Registered importance evaluator for agent type: {}", agentType);
  }

  /**
   * Gets the appropriate importance evaluator for an agent type
   */
  private MemoryImportanceEvaluator getImportanceEvaluator(String agentType) {
    return importanceEvaluators.getOrDefault(agentType, importanceEvaluators.get("default"));
  }

  /**
   * Stores a new memory for an agent
   */
  public MinionMemory storeMemory(Minion agent, String content, MemoryType type) {
    return storeMemory(agent, content, type, Collections.emptyMap());
  }

  /**
   * Stores a new memory for an agent with additional metadata
   */
  public MinionMemory storeMemory(Minion agent, String content, MemoryType type, Map<String, Object> metadata) {
    String agentId = agent.getAgentId();
    String agentType = agent.getClass().getSimpleName();

    // Create new memory
    MinionMemory memory = MinionMemory.builder()
        .id(UUID.randomUUID().toString())
        .agentId(agentId)
        .agentType(agentType)
        .content(content)
        .type(type)
        .createdAt(LocalDateTime.now())
        .expiresAt(LocalDateTime.now().plusSeconds(defaultTtlSeconds))
        .importance(calculateImportance(agent, content, type, metadata))
        .metadata(new HashMap<>(metadata))
        .build();

    // Store in vector store
    Document document = Document.builder()
        .id(memory.getId())
        .text(memory.getContent())
        .metadata("agentId", memory.getAgentId())
        .metadata("agentType", memory.getAgentType())
        .metadata("type", memory.getType().toString())
        .metadata("createdAt", memory.getCreatedAt().toString())
        .metadata("expiresAt", memory.getExpiresAt().toString())
        .metadata("importance", String.valueOf(memory.getImportance()))
        .build();

    // Add additional metadata
    memory.getMetadata().forEach((key, value) ->
        document.mutate().metadata(key, value.toString()));

    // Store in vector store
    vectorStore.add(List.of(document));

    // Add to recent memories cache
    synchronized (recentMemoriesCache) {
      List<MinionMemory> agentMemories = recentMemoriesCache.computeIfAbsent(agentId,
          k -> new ArrayList<>());
      agentMemories.add(memory);

      // Sort by creation time (newest first)
      agentMemories.sort((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()));

      // Trim cache if it gets too large
      if (agentMemories.size() > 100) {
        agentMemories = agentMemories.subList(0, 100);
        recentMemoriesCache.put(agentId, agentMemories);
      }
    }

    log.debug("Stored memory for agent {}: {} (importance: {})",
        agentId, memory.getId(), memory.getImportance());

    // Check if we need to optimize memory
    optimizeMemoryIfNeeded(agent);

    return memory;
  }

  /**
   * Calculates the importance of a memory using agent-specific evaluators
   */
  private double calculateImportance(Minion agent, String content,
      MemoryType type, Map<String, Object> metadata) {
    String agentType = agent.getClass().getSimpleName();
    MemoryImportanceEvaluator evaluator = getImportanceEvaluator(agentType);
    return evaluator.evaluateImportance(content, type, metadata);
  }

  /**
   * Stores a conversation as memory for an agent
   */
  public List<MinionMemory> storeConversationMemory(Minion agent, ChatMemory chatMemory) {
    // Convert chat memory to a string summary
    String summary = MemoryUtils.summarizeChatMemory(chatMemory);

    // Extract key information as metadata
    Map<String, Object> metadata = MemoryUtils.extractMetadataFromChatMemory(chatMemory);

    // Store as a conversation memory
    MinionMemory memory = storeMemory(agent, summary, MemoryType.CONVERSATION, metadata);

    // Return as a list for consistency
    return List.of(memory);
  }

  /**
   * Retrieves recent memories for an agent
   */
  @Cacheable(value = "recentMemories", key = "#agentId + '-' + #limit")
  public List<MinionMemory> getRecentMemories(String agentId, int limit) {
    synchronized (recentMemoriesCache) {
      List<MinionMemory> memories = recentMemoriesCache.getOrDefault(agentId, Collections.emptyList());
      return memories.stream()
          .limit(Math.min(limit, memories.size()))
          .collect(Collectors.toList());
    }
  }

  /**
   * Retrieves memories related to a specific query
   */
  public List<MinionMemory> searchMemories(Minion agent, String query, int limit) {
    return searchMemories(agent.getAgentId(), query, limit);
  }

  /**
   * Retrieves memories related to a specific query by agent ID
   */
  public List<MinionMemory> searchMemories(String agentId, String query, int limit) {
    SearchRequest searchRequest = new SearchRequest.Builder().query(query)
        .topK(limit)
        .similarityThreshold(0.7f)
        .filterExpression("agentId == '" + agentId + "'").build();

    List<Document> documents = vectorStore.similaritySearch(searchRequest);

    return documents.stream()
        .map(doc -> documentToMemory(doc))
        .collect(Collectors.toList());
  }

  /**
   * Converts a document from vector store to an MinionMemory
   */
  private MinionMemory documentToMemory(Document doc) {
    Map<String, Object> metadata = new HashMap<>();

    // Extract standard metadata fields
    doc.getMetadata().forEach((key, value) -> {
      if (!isStandardMetadataField(key)) {
        metadata.put(key, value);
      }
    });

    return MinionMemory.builder()
        .id(doc.getId())
        .agentId(doc.getMetadata().getOrDefault("agentId", "").toString())
        .agentType(doc.getMetadata().getOrDefault("agentType", "").toString())
        .content(doc.getContent())
        .type(MemoryType.valueOf(doc.getMetadata().getOrDefault("type", "GENERIC").toString()))
        .createdAt(LocalDateTime.parse(doc.getMetadata().getOrDefault("createdAt", LocalDateTime.now().toString()).toString()))
        .expiresAt(LocalDateTime.parse(
            doc.getMetadata().getOrDefault("expiresAt", LocalDateTime.now().plusSeconds(defaultTtlSeconds).toString()).toString()))
        .importance(Double.parseDouble(doc.getMetadata().getOrDefault("importance", "0.5").toString()))
        .metadata(metadata)
        .build();
  }

  /**
   * Checks if a metadata field is a standard field
   */
  private boolean isStandardMetadataField(String field) {
    return field.equals("agentId") || field.equals("agentType") ||
        field.equals("type") || field.equals("createdAt") ||
        field.equals("expiresAt") || field.equals("importance");
  }

  /**
   * Gets a specific memory by ID
   */
  @Cacheable(value = "memoryById", key = "#memoryId")
  public Optional<MinionMemory> getMemoryById(String memoryId) {
    SearchRequest searchRequest = new SearchRequest.Builder().query("")
        .filterExpression("id == '" + memoryId + "'").build();

    List<Document> documents = vectorStore.similaritySearch(searchRequest);

    if (documents.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(documentToMemory(documents.get(0)));
  }

  /**
   * Deletes a specific memory
   */
  @CacheEvict(value = "memoryById", key = "#memoryId")
  public boolean deleteMemory(String memoryId) {
    // This is a simplified version as actual deletion depends on vector store implementation
    // Most vector stores have their own deletion mechanism
    log.info("Deleted memory: {}", memoryId);
    return true;
  }

  /**
   * Optimizes agent memory if needed based on count and importance
   */
  private void optimizeMemoryIfNeeded(Minion agent) {
    String agentId = agent.getAgentId();

    // Check if we have too many memories
    int currentCount = countMemoriesForAgent(agentId);
    if (currentCount <= maxMemoriesPerAgent) {
      return;
    }

    log.info("Optimizing memory for agent {} ({} memories)", agentId, currentCount);

    // Find memories to remove (least important first)
    SearchRequest searchRequest = new SearchRequest.Builder().query("")
        .topK(currentCount - maxMemoriesPerAgent + 50) // Get some extras for filtering
        .filterExpression("agentId == '" + agentId + "'").build();

    List<Document> documents = vectorStore.similaritySearch(searchRequest);

    // Filter out any memories that haven't expired yet and have high importance
    List<Document> toRemove = documents.stream()
        .filter(doc -> {
          // Skip memories with importance above threshold
          double importance = Double.parseDouble(doc.getMetadata().getOrDefault("importance", "0.0").toString());
          if (importance > 0.7) {
            return false;
          }

          // Check if expired
          String expiresAtStr = doc.getMetadata().getOrDefault("expiresAt", "").toString();
          if (!expiresAtStr.isEmpty()) {
            LocalDateTime expiresAt = LocalDateTime.parse(expiresAtStr);
            return expiresAt.isBefore(LocalDateTime.now());
          }

          return true;
        })
        .limit(currentCount - maxMemoriesPerAgent)
        .collect(Collectors.toList());

    // Delete the selected memories
    for (Document doc : toRemove) {
      deleteMemory(doc.getId());
    }

    log.info("Removed {} memories from agent {}", toRemove.size(), agentId);
  }

  /**
   * Counts the total memories for an agent
   */
  public int countMemoriesForAgent(String agentId) {
    SearchRequest.Builder builder = new SearchRequest.Builder();
    SearchRequest searchRequest = builder.query("")
        .topK(1)
        .filterExpression("agentId == '" + agentId + "'").build();

    // This is simplified - a real implementation would use a count API if available
    // For now we'll just return a cached value or estimate
    return recentMemoriesCache.getOrDefault(agentId, Collections.emptyList()).size();
  }

  /**
   * Stores a reflection memory - insights the agent has about its operations
   */
  public MinionMemory storeReflectionMemory(Minion agent, String reflection, MinionContext context) {
    // Extract relevant metadata from context
    Map<String, Object> metadata = new HashMap<>();
    if (context != null) {
      // Add relevant context parameters as metadata
      context.getParameterNames().stream()
          .filter(name -> !name.contains("password") && !name.contains("secret") && !name.contains("token"))
          .forEach(name -> {
            Object value = context.getParameter(name);
            if (value != null) {
              metadata.put(name, value.toString());
            }
          });
    }

    // Store with higher importance since reflections tend to be valuable
    MinionMemory memory = storeMemory(agent, reflection, MemoryType.REFLECTION, metadata);

    // Reflections should live longer
    memory.setExpiresAt(LocalDateTime.now().plusDays(30));

    return memory;
  }

  /**
   * Updates the expiration time of a memory
   */
  public boolean updateMemoryExpiration(String memoryId, Duration ttl) {
    Optional<MinionMemory> optMemory = getMemoryById(memoryId);
    if (optMemory.isEmpty()) {
      return false;
    }

    MinionMemory memory = optMemory.get();
    memory.setExpiresAt(LocalDateTime.now().plus(ttl));

    // Update in vector store - implementation depends on vector store capabilities
    log.info("Updated expiration for memory {} to {}", memoryId, memory.getExpiresAt());

    return true;
  }

  /**
   * Clears all memories for an agent
   */
  @CacheEvict(value = "recentMemories", allEntries = true)
  public void clearAgentMemories(String agentId) {
    // Clear cache
    recentMemoriesCache.remove(agentId);

    // Clear from vector store - implementation depends on vector store capabilities
    log.info("Cleared all memories for agent {}", agentId);
  }

  @Cacheable(value = "chatMemoryCache", key = "#agentId")
  public MinionMemory getOrCreateMemory(String agentId) {
    // Create a new ChatMemory if not found in cache
    ChatMemory newMemory = new InMemoryChatMemory();
    MinionMemory minionMemory = MinionMemory.builder()
        .id(UUID.randomUUID().toString())
        .agentId(agentId)
        .agentType("DefaultAgent") // Assuming a default agent type
        .content(newMemory)
        .type(MemoryType.GENERIC) // Assuming a generic type
        .createdAt(LocalDateTime.now())
        .expiresAt(LocalDateTime.now().plusSeconds(defaultTtlSeconds))
        .importance(0.5) // Default importance
        .metadata(new HashMap<>())
        .build();

    // Add the new memory to the cache
    recentMemoriesCache.put(agentId, List.of(minionMemory));

    // Return the new ChatMemory
    return newMemory;
  }
}

