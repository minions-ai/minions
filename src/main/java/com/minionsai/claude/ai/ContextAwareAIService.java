package com.minionsai.claude.ai;

import com.minionsai.claude.context.ExecutionContext;

import java.util.List;
import java.util.Map;

/**
 * Abstraction for AI model interactions with execution context support.
 */
public interface ContextAwareAIService extends AIService {
  /**
   * Generate a text response from a prompt with execution context.
   *
   * @param prompt The text prompt
   * @param context The execution context
   * @return The generated response
   */
  String generateText(String prompt, ExecutionContext context);

  /**
   * Generate a text response from a structured prompt with context.
   *
   * @param systemPrompt Instructions for the AI model
   * @param userPrompt The user's query or input
   * @param context The execution context
   * @return The generated response
   */
  String generateText(String systemPrompt, String userPrompt, ExecutionContext context);

  /**
   * Parse a specific data structure from a text response with context.
   *
   * @param prompt The text prompt that should generate structured data
   * @param responseClass The class to parse the response into
   * @param context The execution context
   * @return The parsed response object
   */
  <T> T generateStructured(String prompt, Class<T> responseClass, ExecutionContext context);
}