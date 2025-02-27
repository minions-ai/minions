package com.minionsai.claude.function;

import com.minionsai.claude.context.ExecutionContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base implementation of MinionFunction that provides
 * common functionality for all Minion functions.
 */
@Slf4j
@Getter
public abstract class AbstractMinionFunction<REQ, RES> implements MinionFunction<REQ, RES> {
  private final String id;
  private final String name;
  private final String description;
  private final String category;
  private final Class<REQ> requestClass;
  private final Class<RES> responseClass;

  protected AbstractMinionFunction(
      String id,
      String name,
      String description,
      String category,
      Class<REQ> requestClass,
      Class<RES> responseClass) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.category = category;
    this.requestClass = requestClass;
    this.responseClass = responseClass;
  }

  /**
   * Validates a request before processing.
   * Subclasses should override this method to provide specific validation.
   *
   * @param request The request to validate
   * @param context The execution context
   * @throws IllegalArgumentException if validation fails
   */
  protected void validateRequest(REQ request, ExecutionContext context) {
    if (request == null) {
      throw new IllegalArgumentException("Request cannot be null");
    }
  }

  /**
   * Template method for executing the function with context.
   * Implements BiFunction.apply.
   */
  @Override
  public RES apply(REQ request, ExecutionContext context) {
    try {
      log.debug("Executing function: {} with context: {}", id, context.getExecutionId());

      // Validate the request
      validateRequest(request, context);

      // Execute the function
      RES response = doExecute(request, context);

      log.debug("Function: {} executed successfully", id);
      return response;
    } catch (Exception e) {
      log.error("Error executing function: {} - {}", id, e.getMessage(), e);
      return handleError(e, request, context);
    }
  }

  /**
   * Execute the function with the given context.
   * Subclasses must implement this method to provide specific functionality.
   *
   * @param request The request to process
   * @param context The execution context
   * @return The function response
   * @throws Exception If execution fails
   */
  protected abstract RES doExecute(REQ request, ExecutionContext context) throws Exception;

  /**
   * Handle errors during execution.
   * Subclasses should override this method to provide specific error handling.
   *
   * @param e The exception that occurred
   * @param request The original request
   * @param context The execution context
   * @return An appropriate error response
   */
  protected abstract RES handleError(Exception e, REQ request, ExecutionContext context);
}