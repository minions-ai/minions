package com.minionsai.claude.function;

import com.minionsai.claude.context.ExecutionContext;

import java.util.function.BiFunction;

/**
 * Base interface for all Functions in the Minions framework.
 * Uses BiFunction to accommodate both the request and context.
 *
 * @param <REQ> The specific request record type
 * @param <RES> The specific response record type
 */
public interface MinionFunction<REQ, RES> extends BiFunction<REQ, ExecutionContext, RES> {
  /**
   * Returns the unique identifier for this function.
   */
  String getId();

  /**
   * Returns the human-readable name of this function.
   */
  String getName();

  /**
   * Returns the description of what this function does.
   */
  String getDescription();

  /**
   * Returns the category this function belongs to.
   */
  String getCategory();

  /**
   * Returns the class of the request type.
   */
  Class<REQ> getRequestClass();

  /**
   * Returns the class of the response type.
   */
  Class<RES> getResponseClass();

  /**
   * Execute with default context. Convenience method.
   */
  default RES execute(REQ request) {
    return apply(request, ExecutionContext.builder().build());
  }
}