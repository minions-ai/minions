package com.minionsai.claude.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionsai.claude.ai.AIService;
import com.minionsai.claude.context.ExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AIService using Spring AI.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpringAIService implements AIService {
  private final ChatClient chatClient;
  private final EmbeddingClient embeddingClient;
  private final ObjectMapper objectMapper;

  @Override
  public String generateText(String prompt) {
    try {
      ChatResponse response = chatClient.call(prompt);
      return response.getResult().getOutput().getContent();
    } catch (Exception e) {
      log.error("Error generating text: {}", e.getMessage(), e);
      throw new AIServiceException("Failed to generate text", e);
    }
  }

  @Override
  public String generateText(String systemPrompt, String userPrompt) {
    try {
      List<Message> messages = new ArrayList<>();
      messages.add(new SystemMessage(systemPrompt));
      messages.add(new UserMessage(userPrompt));

      Prompt prompt = new Prompt(messages);
      ChatResponse response = chatClient.call(prompt);

      return response.getResult().getOutput().getContent();
    } catch (Exception e) {
      log.error("Error generating text with system prompt: {}", e.getMessage(), e);
      throw new AIServiceException("Failed to generate text", e);
    }
  }

  @Override
  public <T> T generateStructured(String prompt, Class<T> responseClass) {
    try {
      // Append instructions to return JSON
      String jsonPrompt = prompt + "\n\nRespond with a valid JSON object that can be parsed as " +
          responseClass.getSimpleName() + ".";

      String jsonResponse = generateText(jsonPrompt);

      // Extract JSON if wrapped in ```json or other markers
      jsonResponse = cleanupJsonResponse(jsonResponse);

      // Parse the JSON response
      return objectMapper.readValue(jsonResponse, responseClass);
    } catch (Exception e) {
      log.error("Error generating structured response: {}", e.getMessage(), e);
      throw new AIServiceException("Failed to generate structured response", e);
    }
  }

  @Override
  public List<Float> getEmbedding(String text) {
    try {
      return embeddingClient.embed(text).getEmbedding();
    } catch (Exception e) {
      log.error("Error generating embedding: {}", e.getMessage(), e);
      throw new AIServiceException("Failed to generate embedding", e);
    }
  }

  @Override
  public Map<String, String> executeReasoning(String prompt) {
    try {
      String reasoningPrompt = "Think step by step to solve this problem. First show your reasoning, " +
          "then provide the final answer.\n\n" + prompt;

      String response = generateText(reasoningPrompt);

      // Simple parsing - would be more robust in production
      Map<String, String> result = new HashMap<>();

      if (response.contains("Final Answer:")) {
        String[] parts = response.split("Final Answer:");
        result.put("reasoning", parts[0].trim());
        result.put("answer", parts[1].trim());
      } else {
        result.put("reasoning", "");
        result.put("answer", response.trim());
      }

      return result;
    } catch (Exception e) {
      log.error("Error executing reasoning: {}", e.getMessage(), e);
      throw new AIServiceException("Failed to execute reasoning", e);
    }
  }

  /**
   * Clean up JSON response by removing markdown code blocks and other non-JSON content.
   */
  private String cleanupJsonResponse(String response) {
    // Remove markdown code blocks if present
    if (response.contains("```json") && response.contains("```")) {
      response = response.substring(
          response.indexOf("```json") + 7,
          response.lastIndexOf("```")
      ).trim();
    } else if (response.contains("```") && response.contains("```")) {
      response = response.substring(
          response.indexOf("```") + 3,
          response.lastIndexOf("```")
      ).trim();
    }

    return response;
  }
}