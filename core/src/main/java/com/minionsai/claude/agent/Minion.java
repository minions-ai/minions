package com.minionsai.claude.agent;

import com.minionsai.claude.agent.memory.MemoryManager;
import com.minionsai.claude.agent.memory.MinionMemory;
import com.minionsai.claude.capability.MinionCapability;
import com.minionsai.claude.context.MinionContext;
import com.minionsai.claude.prompt.PromptComponent;
import com.minionsai.claude.prompt.PromptType;
import com.minionsai.claude.prompt.SystemPrompt;
import com.minionsai.claude.tools.ToolRegistry;
import com.minionsai.claude.workflow.task.Task;
import com.minionsai.claude.workflow.task.TaskResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import reactor.core.publisher.Mono;

@Slf4j
@Data
@Accessors(chain = true)
@SuperBuilder
public abstract class Minion {

  private static final PromptComponent DEFAULT_PROMPT_TEMPLATE = PromptComponent.builder()
      .type(PromptType.REQUEST_TEMPLATE)
      .content("Prompt template content").build();
  // Unique identifier for each agent
  @Builder.Default
  private final String agentId = UUID.randomUUID().toString();
  private final String minionType;
  private final MinionMemory minionMemory;

  // System prompts and configuration
  protected SystemPrompt systemPrompt;

  // Agent metadata
  private String name;
  private String description;
  private MinionState state = MinionState.IDLE;
  // Core components
  private ChatMemory chatMemory;
  private MemoryManager memoryManager;
  private ChatClient chatClient;
  private ToolRegistry toolRegistry;
  // Agent capabilities

  @Builder.Default
  private List<MinionCapability> capabilities = Collections.emptyList();
  // Current context for the agent
  private MinionContext currentContext;

  // Task management
  @Builder.Default
  private Map<String, Task> activeTasks = new ConcurrentHashMap<>();
  private int maxConcurrentTasks = 5;

  // Inter-agent communication
  private MinionRegistry minionRegistry;

  // Metrics and monitoring
  @Builder.Default
  private Map<String, Object> metrics = new ConcurrentHashMap<>();

  /**
   * Constructs a new agent with the necessary components
   */
  public Minion(String minionType, ChatClient.Builder chatClientBuilder, ChatMemory chatMemory,
      MemoryManager memoryManager, ToolRegistry toolRegistry,
      MinionRegistry minionRegistry) {
    agentId = UUID.randomUUID().toString();
    this.minionType = minionType;
    this.minionMemory = memoryManager.getOrCreateMemory(agentId);
    this.chatMemory = minionMemory.getContent();
    this.memoryManager = memoryManager;
    this.toolRegistry = toolRegistry;
    this.minionRegistry = minionRegistry;

    // Load prompt templates
    loadPromptTemplates();

    // Build the chat client with tools and memory
    chatClient = chatClientBuilder
        .defaultSystem(systemPrompt.getFullPromptText())
        .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
        .build();

    // Register this agent with the registry
    if (minionRegistry != null) {
      minionRegistry.registerAgent(this);
    }
  }

  /**
   * Load prompt templates from resources
   */
  protected void loadPromptTemplates() {
    // Default implementation loads from classpath:prompts/{agentType}/*.prompt
    // Subclasses can override this method to customize loading behavior
  }

  /**
   * Returns the path to the main system prompt file
   */
  @Deprecated
  protected abstract String getPromptFilePath();

  /**
   * Process a user request synchronously
   */
  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
  public String processPrompt(String userRequest) {
    try {
      // Update agent state
      setState(MinionState.PROCESSING);

      // Create user message
      Message userMessage = new UserMessage(userRequest);

      // Log the incoming request
      log.debug("Agent {} processing request: {}", agentId, userRequest);

      // Create context if needed
      if (currentContext == null) {
        currentContext = createNewContext();
      }

      // Enrich context with relevant information
      enrichContext(userRequest);

      // Execute the prompt
      String response = chatClient.prompt()
          .messages(List.of(userMessage))
          .call()
          .content();

      // Log the response
      log.info("Agent {} generated response", agentId);
      log.debug("Response content: {}", response);

      // Update metrics
      updateMetrics("promptsProcessed", getMetricValue("promptsProcessed", 0) + 1);

      // Return to idle state
      setState(MinionState.IDLE);

      return response;
    } catch (Exception e) {
      log.error("Error processing prompt", e);
      setState(MinionState.ERROR);
      updateMetrics("errors", getMetricValue("errors", 0) + 1);
      throw e;
    }
  }

  /**
   * Process a user request asynchronously
   */
  public CompletableFuture<String> processPromptAsync(String userRequest) {
    return CompletableFuture.supplyAsync(() -> processPrompt(userRequest));
  }

  /**
   * Process a task asynchronously with Reactor
   */
  public Mono<TaskResult> processTask(Task task) {
    return Mono.fromCallable(() -> {
      // Store task in active tasks
      activeTasks.put(task.getTaskId(), task);

      try {
        // Create a context for this task
        MinionContext taskContext = createContextForTask(task);

        // Check if this agent can handle the task
        if (!canHandleTask(task)) {
          log.info("Agent {} cannot handle task {}", agentId, task.getTaskId());
          return new TaskResult(task.getTaskId(), Status.REJECTED,
              "Agent cannot handle this task type", null);
        }

        // Execute the task
        setState(MinionState.PROCESSING);
        log.info("Agent {} processing task {}", agentId, task.getTaskId());

        // Use templated prompt for task type if available
        PromptTemplate template = getPromptTemplateForTask(task);
        Prompt prompt = template.create(task.getParameters());

        String result = chatClient.prompt(prompt).call().content();

        // Process and validate result
        TaskResult taskResult = new TaskResult(
            task.getTaskId(),
            Status.COMPLETED,
            "Task completed successfully",
            result);

        // Update metrics
        updateMetrics("tasksCompleted", getMetricValue("tasksCompleted", 0) + 1);

        return taskResult;
      } catch (Exception e) {
        log.error("Error processing task " + task.getTaskId(), e);
        updateMetrics("taskErrors", getMetricValue("taskErrors", 0) + 1);
        return new TaskResult(task.getTaskId(), Status.FAILED, e.getMessage(), null);
      } finally {
        // Remove from active tasks and return to idle state
        activeTasks.remove(task.getTaskId());
        if (activeTasks.isEmpty()) {
          setState(MinionState.IDLE);
        }
      }
    });
  }

  /**
   * Determine if this agent can handle a given task
   */
  public boolean canHandleTask(Task task) {
    // Check if the agent has the capabilities required for this task
    return capabilities.stream()
        .anyMatch(capability -> capability.canHandle(task.getType()));
  }

  /**
   * Get the registered tools for this agent
   */
  protected FunctionCallback[] getRegisteredTools() {
    if (toolRegistry == null) {
      return getAvailableTools();
    }
    return toolRegistry.getToolsForAgent(this.getClass().getSimpleName());
  }

  /**
   * Get the available tools defined by the agent implementation
   */
  protected abstract FunctionCallback[] getAvailableTools();

  /**
   * Updates the system prompt dynamically
   */
  public void updateSystemPrompt(SystemPrompt newPrompt) {
    this.systemPrompt = newPrompt;
    chatClient.prompt(new Prompt(new SystemMessage(newPrompt.getFullPromptText())));
  }

  /**
   * Creates a new context for the agent
   */
  protected MinionContext createNewContext() {
    return new MinionContext();
  }

  /**
   * Creates a context for a specific task
   */
  protected MinionContext createContextForTask(Task task) {
    MinionContext context = createNewContext();
    context.addParameter("taskId", task.getTaskId());
    context.addParameter("taskType", task.getType());
    return context;
  }

  /**
   * Enriches the current context with relevant information
   */
  protected void enrichContext(String userRequest) {
    if (currentContext != null) {
      currentContext.addParameter("lastUserRequest", userRequest);
      currentContext.addParameter("timestamp", String.valueOf(System.currentTimeMillis()));
    }
  }

  /**
   * Gets a prompt template for a specific task
   */
  protected PromptTemplate getPromptTemplateForTask(Task task) {
    String templateKey = task.getType();
    String templateContent = systemPrompt.getComponents().stream()
        .filter(promptComponent -> promptComponent.getType() == PromptType.REQUEST_TEMPLATE).findFirst()
        .orElse(DEFAULT_PROMPT_TEMPLATE)
        .getContent();
    return new PromptTemplate(templateContent);
  }

  /**
   * Sends a message to another agent
   */
  public CompletableFuture<String> sendMessageToAgent(String targetAgentId, String message) {
    if (minionRegistry == null) {
      return CompletableFuture.failedFuture(
          new IllegalStateException("Agent registry not configured"));
    }

    Minion targetAgent = minionRegistry.getAgent(targetAgentId);
    if (targetAgent == null) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("Target agent not found: " + targetAgentId));
    }

    return targetAgent.processPromptAsync(message);
  }

  /**
   * Get current agent state
   */
  public MinionState getState() {
    return state;
  }

  /**
   * Set agent state
   */
  protected void setState(MinionState newState) {
    MinionState oldState = this.state;
    this.state = newState;
    log.debug("Agent {} state changed: {} -> {}", agentId, oldState, newState);
  }

  /**
   * Update metric value
   */
  protected void updateMetrics(String key, Object value) {
    metrics.put(key, value);
  }

  /**
   * Get metric value with default
   */
  @SuppressWarnings("unchecked")
  protected <T> T getMetricValue(String key, T defaultValue) {
    return (T) metrics.getOrDefault(key, defaultValue);
  }

  /**
   * Agent state enum
   */
  public enum MinionState {
    IDLE,
    PROCESSING,
    WAITING,
    ERROR
  }
}