package com.minionsai.claude.tools;

import com.minionsai.claude.exceptions.ToolExecutionException;
import com.minionsai.claude.function.MinionFunction;

/**
 * Represents a tool capability that Minions can use to interact with
 * external systems or perform specific actions.
 */
public interface Tool<REQ, RES> extends MinionFunction<REQ, RES> {
  /**
   * Execute the tool with the provided request.
   * Implementation of Function.apply from java.util.function.Function
   *
   * @param request The tool request
   * @return The tool response
   */
  @Override
  default RES apply(REQ request) {
    try {
      return execute(request);
    } catch (ToolExecutionException e) {
      return createErrorResponse(e);
    }
  }

  /**
   * Execute the tool with the provided request.
   *
   * @param request The tool request
   * @return The tool response
   * @throws ToolExecutionException If the tool execution fails
   */
  RES execute(REQ request) throws ToolExecutionException;

  /**
   * Create an error response when execution fails.
   *
   * @param e The exception that occurred
   * @return An error response
   */
  RES createErrorResponse(ToolExecutionException e);
}