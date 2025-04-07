package com.minionslab.core.domain;

import com.minionslab.core.context.MinionContext;
import com.minionslab.core.context.MinionContextHolder;
import com.minionslab.core.domain.enums.MinionState;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.tools.ToolRegistry;
import com.minionslab.core.event.MinionEventPublisher;
import com.minionslab.core.event.MinionStateChangedEvent;
import com.minionslab.core.service.LLMService;
import com.minionslab.core.service.impl.llm.model.LLMRequest;
import com.minionslab.core.service.impl.llm.model.LLMResponse;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Builder.Default;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
@Data
@Accessors(chain = true)
@SuperBuilder
public abstract class AbstractMinion extends BaseEntity implements MinionLifecycle {


  // Unique identifier for each agent
  @Default private final String minionId = UUID.randomUUID().toString();

  // Add event publisher
  @NotNull
  private final MinionEventPublisher eventPublisher;

  // System prompts and configuration
  @Setter @Getter protected MinionPrompt minionPrompt;

  // Agent metadatas
  private String description;
  @Default private MinionState state = MinionState.CREATED;

  // Core components

  @Setter(AccessLevel.PACKAGE) private ToolRegistry toolRegistry;

  // Metrics and monitoring
  @Default private Map<String, Object> metrics = new ConcurrentHashMap<>();
  @NotNull private MinionType minionType;

  @Default
  private List<String> toolboxNames = new ArrayList<>();
  @Default
  private Map<String, Object> toolboxes = new ConcurrentHashMap<>();
  @Default
  private Map<String, Object> metadata = new ConcurrentHashMap<>();

  @NotNull
  private LLMService llmService;

  @NotNull
  private MinionRecipe recipe;


  /**
   * Get the available tools defined by the agent implementation
   */
  protected abstract FunctionCallback[] getAvailableTools();

  @Override public void initialize() {
    // Set up parameters for this operation
    changeState(MinionState.INITIALIZING);
    try {
      // Load registered tools
      changeState(MinionState.IDLE);
    } catch (Exception e) {
      changeState(MinionState.ERROR);
      throw e;
    }
  }

  @Override public void start() {
    changeState(MinionState.STARTED);
  }

  @Override public void stop() {
    changeState(MinionState.STOPPED);
  }

  @Override public void pause() {
    changeState(MinionState.WAITING);
  }

  @Override public void resume() {
    changeState(MinionState.IDLE);
  }

  @Override public void resumeProcessing() {
    validateState(MinionState.WAITING, "Cannot resume processing from current state: " + state);
    changeState(MinionState.PROCESSING);
  }

  @Override public void pauseProcessing() {
    validateState(MinionState.PROCESSING, "Cannot pause processing from current state: " + state);
    changeState(MinionState.WAITING);
  }

  @Override public void recover() {
    validateState(MinionState.ERROR, "Cannot recover from current state: " + state);
    changeState(MinionState.IDLE);
  }

  @Override public void reinitialize() {
    validateState(MinionState.ERROR, "Cannot reinitialize from current state: " + state);
    initialize();
  }

  @Override public void startProcessing() {
    validateState(MinionState.STARTED, "Cannot start processing from current state: " + state);
    changeState(MinionState.IDLE);
  }

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
      this.metadata.putAll(parameters);

      LLMRequest request = LLMRequest.builder()
          .prompt(this.minionPrompt)
          .userRequest(userRequest)
          .metadata(metadata)
          .build();

      LLMResponse llmResponse = llmService.processRequest(request);

      // Log the response
      log.info("Agent {} generated response", minionId);
      log.debug("Response text: {}", llmResponse.getResponseText());

      // Update metrics
      updateMetrics("promptsProcessed", getMetricValue("promptsProcessed", 0) + 1);

      return llmResponse.getResponseText();
    } catch (Exception e) {
      log.error("Error processing prompt", e);
      handleFailure(e);
      throw e;
    } finally {
      // Return to idle state and clear parameters
      changeState(MinionState.IDLE);
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
      // Publish state change event instead of directly notifying listeners
      eventPublisher.publishEvent(MinionStateChangedEvent.of(getMinionId(), oldState, newState));
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
      case IDLE -> to == MinionState.PROCESSING || to == MinionState.WAITING || to == MinionState.SHUTTING_DOWN;
      case PROCESSING -> to == MinionState.IDLE || to == MinionState.WAITING || to == MinionState.ERROR;
      case WAITING -> to == MinionState.IDLE || to == MinionState.PROCESSING || to == MinionState.ERROR;
      case ERROR -> to == MinionState.IDLE || to == MinionState.INITIALIZING || to == MinionState.SHUTTING_DOWN;
      case SHUTTING_DOWN -> to == MinionState.SHUTDOWN;
      case SHUTDOWN -> false; // No transitions from SHUTDOWN
      case STARTED -> to == MinionState.IDLE || to == MinionState.STOPPED;
      case STOPPED -> to == MinionState.SHUTTING_DOWN;
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