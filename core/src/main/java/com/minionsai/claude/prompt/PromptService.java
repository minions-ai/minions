package com.minionsai.claude.prompt;


import com.minionsai.claude.agent.factory.MinionType;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for managing system prompts using both document DB and vector store
 */
@Service
@Slf4j
@AllArgsConstructor
public class PromptService {

  @Autowired
  private final SystemPromptRepository promptRepository;

  @Autowired
  private final VectorStore vectorStore;

  @Autowired
  private final EmbeddingModel embeddingClient;

  /**
   * Save a system prompt with all its components Components are stored both in document DB and vector store
   */
  public SystemPrompt saveSystemPrompt(SystemPrompt prompt) {
    // Set timestamps
    long now = System.currentTimeMillis();
    if (prompt.getCreatedAt() == 0) {
      prompt.setCreatedAt(now);
    }
    prompt.setUpdatedAt(now);

    // Store prompt components in vector store
    for (PromptComponent component : prompt.getComponents()) {
      if (component.getEmbeddingId() == null || component.getEmbeddingId().isEmpty()) {
        // Create embedding and store in vector DB
        Document document = Document.builder()
            .id(component.getId())
            .text(component.getContent())
            .metadata("promptId", prompt.getId())
            .metadata("componentName", component.getName())
            .metadata("agentType", prompt.getAgentType())
            .metadata("type", component.getType())
            .build();

        // Store in vector DB
        vectorStore.add(List.of(document));

        // Update component with embedding ID
        component.setEmbeddingId(component.getId());
      }
    }

    // Save to document DB
    return promptRepository.save(prompt);
  }

  /**
   * Get a system prompt by ID
   */
  public Optional<SystemPrompt> getSystemPromptById(String id) {
    return promptRepository.findById(id);
  }

  /**
   * Get the latest version of a prompt for an agent type
   */
  public Optional<SystemPrompt> getLatestPromptForAgentType(MinionType agentType) {
    return promptRepository.findLatestByAgentType(agentType);
  }

  /**
   * Find all prompts for a specific agent type
   */
  public List<SystemPrompt> findByAgentType(String agentType) {
    return promptRepository.findByAgentType(agentType);
  }

  /**
   * Find similar prompt components based on a query
   */
  public List<Document> findSimilarPromptComponents(String query, String agentType, int limit) {
    SearchRequest searchRequest = new SearchRequest.Builder().query(query)
        .topK(limit)
        .similarityThreshold(0.7f)
        .filterExpression("agentType == '" + agentType + "'").build();

    return vectorStore.similaritySearch(searchRequest);
  }

  /**
   * Dynamically build a system prompt based on a query and agent type
   */
  public SystemPrompt buildDynamicPrompt(String agentType, String query) {
    // Get the base prompt for this agent type
    SystemPrompt basePrompt = promptRepository.findLatestByAgentType(agentType)
        .orElseThrow(() -> new IllegalArgumentException("No prompt found for agent type: " + agentType));

    // Find similar components from vector store
    List<Document> similarComponents = findSimilarPromptComponents(query, agentType, 5);

    // Create a new prompt with the base components
    SystemPrompt dynamicPrompt = new SystemPrompt();
    dynamicPrompt.setName("Dynamic-" + basePrompt.getName());
    dynamicPrompt.setAgentType(agentType);
    dynamicPrompt.setComponents(basePrompt.getComponents());

    // Add relevant components from vector search
    for (Document doc : similarComponents) {
      PromptComponent component = PromptComponent.builder()
          .id(doc.getId())
          .name(doc.getMetadata().getOrDefault("componentName", "dynamic-component").toString())
          .content(doc.getContent())
          .type(PromptType.DYNAMIC)
          .embeddingId(doc.getId())
          .weight(doc.getScore())
          .order(100.0) // Place dynamic components after base components
          .build();

      dynamicPrompt.addComponent(component);
    }

    return dynamicPrompt;
  }
}

