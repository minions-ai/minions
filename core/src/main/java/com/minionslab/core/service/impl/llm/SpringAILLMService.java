package com.minionslab.core.service.impl.llm;

import com.minionslab.core.domain.ChateMemoryStrategy;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.MinionRegistry;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.domain.tools.ToolBox;
import com.minionslab.core.domain.tools.ToolRegistry;
import com.minionslab.core.exception.LLMServiceException;
import com.minionslab.core.service.LLMService;
import com.minionslab.core.service.impl.llm.model.LLMRequest;
import com.minionslab.core.service.impl.llm.model.LLMResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.minionslab.core.service.ChatMemoryFactory;
import com.minionslab.core.domain.ChatMemory;

/**
 * Default implementation of the LLMService interface. This service is responsible for processing requests using LLM models.
 */
@Slf4j
@Service
public class SpringAILLMService implements LLMService {

  private final ChatClient.Builder chatClientBuilder;
  private final ToolRegistry toolRegistry;
  private final MinionRegistry minionRegistry;
  private final ChatMemoryFactory chatMemoryFactory;

  @Autowired
  public SpringAILLMService(
      ChatClient.Builder chatClientBuilder,
      ToolRegistry toolRegistry, 
      MinionRegistry minionRegistry,
      ChatMemoryFactory chatMemoryFactory) {
    this.chatClientBuilder = chatClientBuilder;
    this.toolRegistry = toolRegistry;
    this.minionRegistry = minionRegistry;
    this.chatMemoryFactory = chatMemoryFactory;
  }

  @Override
  public LLMResponse processRequest(LLMRequest request) {
    log.debug("Processing LLM request: {}", request);
    String requestId = UUID.randomUUID().toString();

    try {
      // Validate inputs
      validateInputs(request);

      Minion minion = minionRegistry.getMinion(request.getMinionId());

      List<ChateMemoryStrategy> memoryStrategies = minion.getRecipe().getMemoryStrategies();

      // Get function definitions from all enabled toolboxes
      List<ToolBox> tools = getToolboxes(request.getPrompt().getToolboxes());

      // Build system message with all prompt components
      String systemPrompt = buildSystemPrompt(request.getPrompt());
      SystemMessage systemMessage = new SystemMessage(systemPrompt);

      // Build user message with template processing
      String userPrompt = buildUserPrompt(request.getPrompt(), request);
      UserMessage userMessage = new UserMessage(userPrompt, new ArrayList<>(), request.getMetadata());

      // Configure ChatClient with all necessary components
      ChatClient chatClient = configureChatClient(request.getPrompt(), tools,memoryStrategies);

      // Process the request
      String responseText = chatClient.prompt()
          .messages(List.of(systemMessage, userMessage))
          .call()
          .content();

      log.debug("Request [{}] processed successfully", requestId);

      // Create and return the LLM response
      return LLMResponse.builder()
          .requestId(requestId)
          .promptId(request.getPrompt().getId())
          .promptVersion(request.getPrompt().getVersion())
          .responseText(responseText)
          .timestamp(Instant.now())
          .build();
    } catch (Exception e) {
      log.error("Error processing LLM request: {}", request, e);
      throw new LLMServiceException("Failed to process LLM request", e);
    }
  }

  private List<ToolBox> getToolboxes(Set<String> toolboxNames) {
    List<ToolBox> toolBoxes = new ArrayList<>();
    toolboxNames.forEach(toolboxName -> {
      toolRegistry.getToolBoxByName(toolboxName).ifPresent(toolBox -> {
        toolBoxes.add(toolBox);
      });
    });
    return toolBoxes;
  }


  /**
   * Configures the ChatClient with the given prompt and function callbacks.
   *
   * @param prompt           the prompt
   * @param tools            the function callbacks
   * @param memoryStrategies
   * @return a configured ChatClient
   */
  private ChatClient configureChatClient(MinionPrompt prompt, List<ToolBox> tools, List<ChateMemoryStrategy> memoryStrategies) {
    // Create chat options
    ChatOptions chatOptions = ChatOptions.builder()
        .temperature(0.7)
        .build();



    // Build and return configured ChatClient
    ChatClient.Builder builder = chatClientBuilder
        .defaultSystem(prompt.getDescription())
        .defaultOptions(chatOptions);

    // Add function callbacks if any
    if (!tools.isEmpty()) {
      builder.defaultTools(tools);
    }

    return builder.build();
  }

  private List<Advisor> getMemoryAdvisors(List<ChateMemoryStrategy> strategies) {
    List<Advisor> advisors = new ArrayList<>();
    for (ChateMemoryStrategy strategy : strategies) {
      ChatMemory chatMemory = chatMemoryFactory.createChatMemory(strategy.getType());
      
      switch (strategy.getType()) {
        case PROMPT:
          advisors.add(new PromptChatMemoryAdvisor(chatMemory));
          break;
        case VECTOR:
          // Add vector memory advisor when implemented
          break;
        case MESSAGE:
          // Add message memory advisor when implemented
          break;
      }
    }
    return advisors;
  }

  private void validateInputs(LLMRequest request) {
    if (request == null) {
      throw new LLMServiceException("Request cannot be null");
    }
    if (request.getPrompt() == null) {
      throw new LLMServiceException("Prompt cannot be null");
    }
    if (request.getUserRequest() == null || request.getUserRequest().trim().isEmpty()) {
      throw new LLMServiceException("User request cannot be null or empty");
    }
  }

  private String buildSystemPrompt(MinionPrompt prompt) {
    return prompt.getComponents().entrySet().stream()
        .filter(entry -> entry.getKey() == PromptType.SYSTEM)
        .map(entry -> entry.getValue().getText())
        .collect(Collectors.joining("\n"));
  }

  private String buildUserPrompt(MinionPrompt prompt, LLMRequest request) {
    String template = prompt.getComponents().entrySet().stream()
        .filter(entry -> entry.getKey() == PromptType.USER_TEMPLATE)
        .map(entry -> entry.getValue().getText())
        .collect(Collectors.joining("\n"));

    return template.replace("${userRequest}", request.getUserRequest());
  }
}