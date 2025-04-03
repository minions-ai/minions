package com.minionslab.core.domain;

import com.minionslab.core.domain.enums.MinionState;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.domain.tools.ToolRegistry;
import com.minionslab.core.service.MinionLifecycleListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
@Data
@Accessors(chain = true)
@SuperBuilder
public abstract class AbstractMinion  extends BaseEntity implements MinionLifecycle {



  private static final PromptComponent DEFAULT_PROMPT_TEMPLATE = PromptComponent.builder().type(PromptType.REQUEST_TEMPLATE)
      .text("Prompt template text").build();

  // Unique identifier for each agent
  @Builder.Default private final String minionId = UUID.randomUUID().toString();

  @Getter(AccessLevel.PROTECTED) private final List<MinionLifecycleListener> lifecycleListeners = new ArrayList<>();

  // System prompts and configuration
  @Setter @Getter protected MinionPrompt minionPrompt;

  // Agent metadatas
  private String description;
  @Default private MinionState state = MinionState.CREATED;

  // Core components
  @Setter(AccessLevel.PACKAGE) private ChatMemory chatMemory;
  private ChatClient chatClient;
  @Setter(AccessLevel.PACKAGE) private ToolRegistry toolRegistry;

  // Metrics and monitoring
  @Default private Map<String, Object> metrics = new ConcurrentHashMap<>();
  @Getter @Setter private MinionType minionType;
  private List<String> toolboxNames = new ArrayList<>();
  private Map<String, Object> toolboxes = new ConcurrentHashMap<>();
  @Getter @Setter private Map<String, Object> metadata;


  /**
   * Get the available tools defined by the agent implementation
   */
  protected abstract FunctionCallback[] getAvailableTools();

  @Override public void initialize() {
    // Set up parameters for this operation
    changeState(MinionState.INITIALIZING);
    try {
      // Load registered tools
      loadRegisteredTools();
      changeState(MinionState.IDLE);
    } catch (Exception e) {
      changeState(MinionState.ERROR);
      throw e;
    }
  }

  @Override public void start() {
    changeState(MinionState.IDLE);
  }

  @Override public void pause() {
    changeState(MinionState.WAITING);
  }

  @Override public void resume() {
    changeState(MinionState.IDLE);
  }

  protected void loadRegisteredTools() {
    for (String toolboxName : toolboxNames) {
      Object toolbox = toolRegistry.getToolbox(toolboxName);
      this.toolboxes.put(toolboxName, toolbox);
    }
  }

  /**
   * Process a user request synchronously
   */
  /*
  * //todo:
  *    1- Handle reactive execution
  *    2- Handle tool context creation if needed from MinionContext
  *    3-  Hanlde structured output
  *
  * */
  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
  public String processPrompt(String userRequest, Map<String, Object> parameters) {
    if (userRequest == null || userRequest.trim().isEmpty()) {
      throw new IllegalArgumentException("User request cannot be null or empty");
    }

    try {
      // Validate state
      validateState(MinionState.IDLE, "Cannot process prompt in current state: " + state);

      // Update agent state
      changeState(MinionState.PROCESSING);

      // Create user message
      Message userMessage = new UserMessage(userRequest);

      // Log the incoming request
      log.debug("Agent {} processing request: {}", minionId, userRequest);

      enrichContext(userRequest);

      // Execute the prompt
      String response = chatClient.prompt().messages(List.of(userMessage)).call().content();

      // Log the response
      log.info("Agent {} generated response", minionId);
      log.debug("Response text: {}", response);

      // Update metrics
      updateMetrics("promptsProcessed", getMetricValue("promptsProcessed", 0) + 1);

      return response;
    } catch (Exception e) {
      log.error("Error processing prompt", e);
      handleFailure(e);
      throw e;
    } finally {
      // Return to idle state and clear parameters
      changeState(MinionState.IDLE);
      MinionContextHolder.clearContext();
    }
  }

  /**
   * Process a user request asynchronously
   */
  public CompletableFuture<String> processPromptAsync(String userRequest, Map<String, Object> parameters) {
    return CompletableFuture.supplyAsync(() -> processPrompt(userRequest, parameters));
  }



  /**
   * Get current agent state
   */
  @Override public MinionState getState() {
    return state;
  }

  /**
   * Set agent state with validation
   */
  private void changeState(MinionState newState) {
    MinionState oldState = this.state;
    if (isValidStateTransition(oldState, newState)) {
      this.state = newState;
      fireLifecycleEvent(oldState, newState);
    } else {
      throw new IllegalStateException(String.format("Invalid state transition from %s to %s", oldState, newState));
    }
  }

  /**
   * Check if a state transition is valid
   */
  private boolean isValidStateTransition(MinionState from, MinionState to) {
    return switch (from) {
      case CREATED -> to == MinionState.INITIALIZING || to == MinionState.ERROR;
      case INITIALIZING -> to == MinionState.IDLE || to == MinionState.ERROR;
      case IDLE -> to == MinionState.PROCESSING || to == MinionState.WAITING;
      case PROCESSING -> to == MinionState.IDLE || to == MinionState.ERROR;
      case WAITING -> to == MinionState.IDLE || to == MinionState.ERROR;
      case ERROR -> to == MinionState.IDLE || to == MinionState.SHUTTING_DOWN;
      case SHUTTING_DOWN -> to == MinionState.SHUTDOWN;
      case SHUTDOWN -> false; // No transitions from SHUTDOWN
    };
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
  @SuppressWarnings("unchecked") protected <T> T getMetricValue(String key, T defaultValue) {
    return (T) metrics.getOrDefault(key, defaultValue);
  }

  /**
   * Validate the current state for an operation
   */
  protected void validateState(MinionState expectedState, String errorMessage) {
    if (state != expectedState) {
      throw new IllegalStateException(errorMessage);
    }
  }

  /**
   * Shutdown the minion gracefully
   */
  @Override public void shutdown() {
    try {
      changeState(MinionState.SHUTTING_DOWN);
      changeState(MinionState.SHUTDOWN);
    } catch (Exception e) {
      log.error("Error during shutdown of minion: {}", minionId, e);
      throw new IllegalStateException("Failed to shutdown minion", e);
    }
  }

  /**
   * Handle a failure in the minion
   */
  @Override public void handleFailure(Exception error) {
    try {
      changeState(MinionState.ERROR);
      updateMetrics("errors", getMetricValue("errors", 0) + 1);
    } catch (Exception e) {
      log.error("Error handling failure in minion: {}", minionId, e);
      throw new IllegalStateException("Failed to handle minion failure", e);
    }
  }

  protected void fireLifecycleEvent(MinionState oldState, MinionState newState) {
    MinionLifecycleEvent event = MinionLifecycleEvent.builder().minionId(this.getMinionId()).oldState(oldState).newState(newState)
        .timestamp(Instant.now()).metadata(Collections.emptyMap()).build();

    lifecycleListeners.forEach(listener -> {
      try {
        listener.onStateChange(event);
      } catch (Exception e) {
        log.error("Error notifying lifecycle listener", e);
      }
    });
  }

  /**
   * Enriches the current parameters with relevant information
   */
  protected void enrichContext(String userRequest) {
    MinionContext context = MinionContextHolder.getContext();
    if (context != null) {
      context.addMetadata("lastUserRequest", userRequest);
      context.addMetadata("timestamp", System.currentTimeMillis());
      context.addMetadata("state", state);
    }
  }

  public String getVersion() {
    return minionPrompt.getVersion();
  }

  public void updateMetadata(Map<String, String> metadata) {
    //todo complete this method
  }


}