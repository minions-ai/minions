package com.minionsai.claude.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionsai.claude.ai.ContextAwareAIService;
import com.minionsai.claude.context.ExecutionContext;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced implementation of AIService with monitoring, retry logic, and context awareness.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedAIService implements ContextAwareAIService {
  private final ChatClient chatClient;
  private final EmbeddingClient embeddingClient;
  private final ObjectMapper objectMapper;
  private final MeterRegistry meterRegistry;

  @Override
  public String generateText(String prompt) {
    return generateText(prompt, ExecutionContext.builder().build());
  }

  @Override
  public String generateText(String systemPrompt, String userPrompt) {
    return generateText(systemPrompt, userPrompt, ExecutionContext.builder().build());
  }

  @Override
  @Retryable(
      value = {AIServiceException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public String generateText(String prompt, ExecutionContext context) {
    Timer.Sample sample = Timer.start(meterRegistry);
    String modelName = chatClient.getClass().getSimpleName();

    try {
      log.debug("Generating text with prompt length: {} [context: {}]",
          prompt.length(), context.getExecutionId());

      ChatResponse response = chatClient.call(prompt);
      String result = response.getResult().getOutput().getContent();

      log.debug("Generated response length: {} [context: {}]",
          result.length(), context.getExecutionId());

      return result;
    } catch (Exception e) {
      log.error("Error generating text [context: {}]: {}",
          context.getExecutionId(), e.getMessage(), e);

      meterRegistry.counter("ai.error",
          "model", modelName,
          "type", e.getClass().getSimpleName()).increment();

      throw new AIServiceException("Failed to generate text", e);
    } finally {
      sample.stop(Timer.builder("ai.request.duration")
          .tag("model", modelName)
          .tag("operation", "generateText")
          .publishPercentileHistogram()
          .register(meterRegistry));
    }
  }

  @Override
  public String generateText(String systemPrompt, String userPrompt, ExecutionContext context) {
    Timer.Sample sample = Timer.start(meterRegistry);
    String modelName = chatClient.getClass().getSimpleName();

    try {
      List<Message> messages = new ArrayList<>();
      messages.add(new SystemMessage(systemPrompt));
      messages.add(new UserMessage(userPrompt));

      // Add user/tenant context as metadata
      Map<String, Object> metadata = new HashMap<>();
      if (context.getUserId() != null) {
        metadata.put("userId", context.getUserId());
      }
      if (context.getTenantId() != null) {
        metadata.put("tenantId", context.getTenantId());
      }

      Prompt prompt = new Prompt(messages, metadata);
      ChatResponse response = chatClient.call(prompt);

      return response.getResult().getOutput().getContent();
    } catch (Exception e) {
      log.error("Error generating text with system prompt [context: {}]: {}",
          context.getExecutionId(), e.getMessage(), e);

      meterRegistry.counter("ai.error",
          "model", modelName,
          "type", e.getClass().getSimpleName()).increment();

      throw new AIServiceException("Failed to generate text", e);
    } finally {
      sample.stop(Timer.builder("ai.request.duration")
          .tag("model", modelName)
          .tag("operation", "generateTextWithSystem")
          .publishPercentileHistogram()
          .register(meterRegistry));
    }
  }

  @Override
  public <T> T generateStructured(String prompt, Class<T> responseClass) {
    return generateStructured(prompt, responseClass, ExecutionContext.builder().build());
  }

  @Override
  @Retryable(
      value = {AIServiceException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public <T> T generateStructured(String prompt, Class<T> responseClass, ExecutionContext context) {
    Timer.Sample sample = Timer.start(meterRegistry);
    String modelName = chatClient.getClass().getSimpleName();

    try {
      // Enhance the prompt with specific instructions for structured output
      String enhancedPrompt = String.format(
          "You must respond with a valid JSON object that follows this schema:\n" +
              "%s\n\nInput: %s\n\nRespond with a JSON object and nothing else.",
          getSchemaDescription(responseClass),
          prompt
      );

      String jsonResponse = generateText(enhancedPrompt, context);

      // Extract JSON if wrapped in ```json or other markers
      jsonResponse = cleanupJsonResponse(jsonResponse);

      // Parse the JSON response
      return objectMapper.readValue(jsonResponse, responseClass);
    } catch (Exception e) {
      log.error("Error generating structured response [context: {}]: {}",
          context.getExecutionId(), e.getMessage(), e);

      meterRegistry.counter("ai.error",
          "model", modelName,
          "type", e.getClass().getSimpleName(),
          "operation", "generateStructured").increment();

      throw new AIServiceException("Failed to generate structured response", e);
    } finally {
      sample.stop(Timer.builder("ai.request.duration")
          .tag("model", modelName)
          .tag("operation", "generateStructured")
          .tag("responseType", responseClass.getSimpleName())
          .publishPercentileHistogram()
          .register(meterRegistry));
    }
  }

  /**
   * Generate a simple schema description for the response class.
   */
  private String getSchemaDescription(Class<?> responseClass) {
    // This is a simplified example - in production you'd use reflection
    // or JSON Schema generation to create a proper schema description
    return "A " + responseClass.getSimpleName() + " object with appropriate fields";
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

  // Implementations of other methods...
}