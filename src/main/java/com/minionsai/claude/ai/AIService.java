package com.minionsai.claude.ai;

import java.util.List;
import java.util.Map;

/**
 * Abstraction for AI model interactions, decoupling the Minions framework
 * from specific AI provider implementations.
 */
public interface AIService {
  /**
   * Generate a text response from a prompt.
   *
   * @param prompt The text prompt
   * @return The generated response
   */
  String generateText(String prompt);

  /**
   * Generate a text response from a structured prompt with system and user messages.
   *
   * @param systemPrompt Instructions for the AI model
   * @param userPrompt The user's query or input
   * @return The generated response
   */
  String generateText(String systemPrompt, String userPrompt);

  /**
   * Parse a specific data structure from a text response.
   *
   * @param prompt The text prompt that should generate structured data
   * @param responseClass The class to parse the response into
   * @return The parsed response object
   */
  <T> T generateStructured(String prompt, Class<T> responseClass);

  /**
   * Get embeddings for a text input.
   *
   * @param text The text to embed
   * @return The embedding vector
   */
  List<Float> getEmbedding(String text);

  /**
   * Execute a chain of thought reasoning process and return
   * both the final answer and the reasoning steps.
   *
   * @param prompt The reasoning prompt
   * @return Map containing the answer and reasoning steps
   */
  Map<String, String> executeReasoning(String prompt);
}