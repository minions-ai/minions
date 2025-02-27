package com.minionsai.claude.function.impl;

import com.minionsai.claude.context.ExecutionContext;
import com.minionsai.claude.function.AbstractMinionFunction;
import com.minionsai.claude.function.impl.SummarizeFunction.SummarizeRequest;
import com.minionsai.claude.function.impl.SummarizeFunction.SummarizeResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Response record for text summarization.
 */


/**
 * A function that summarizes text.
 */
@Slf4j
public class SummarizeFunction extends AbstractMinionFunction<SummarizeRequest, SummarizeResponse> {

  private final ChatClient chatClient;

  public SummarizeFunction(ChatClient chatClient) {
    super(
        "summarize-text",
        "Text Summarizer",
        "Summarizes text to a specified maximum length, optionally including bullet points.",
        "text-processing",
        SummarizeRequest.class,
        SummarizeResponse.class
    );
    this.chatClient = chatClient;
  }

  @Override
  protected void validateRequest(SummarizeRequest request, ExecutionContext context) {
    super.validateRequest(request, context);

    if (request.text() == null || request.text().isEmpty()) {
      throw new IllegalArgumentException("Text cannot be empty");
    }

    if (request.maxLength() <= 0) {
      throw new IllegalArgumentException("Max length must be positive");
    }

    // Example of context-based validation
    if (context.getUserId() == null && context.getAttribute("allowAnonymous").isEmpty()) {
      throw new IllegalArgumentException("Anonymous summarization not allowed");
    }
  }

  @Override
  protected SummarizeResponse doExecute(SummarizeRequest request, ExecutionContext context) throws Exception {
    // Use context to enhance prompt if needed
    String languagePreference = context.getAttribute("languagePreference")
        .map(Object::toString)
        .orElse("en");

    // Use context for personalization
    String userPreferredStyle = context.getAttribute("writingStyle")
        .map(Object::toString)
        .orElse("neutral");

    String prompt = String.format(
        "Summarize the following text in %s style with no more than %d words%s. Output language: %s\n\n%s",
        userPreferredStyle,
        request.maxLength(),
        request.includeBulletPoints() ? " and include up to 5 bullet points of key takeaways" : "",
        languagePreference,
        request.text()
    );

    log.debug("User {} requesting summary with style: {}",
        context.getUserId(), userPreferredStyle);

    String aiResponse = chatClient.call(prompt).getResult().getOutput().getContent();

    // Parse the response - this is simplified
    String summary;
    List<String> bulletPoints = new ArrayList<>();

    if (request.includeBulletPoints() && aiResponse.contains("Key Takeaways:")) {
      // Simple parsing logic - would be more robust in production
      String[] parts = aiResponse.split("Key Takeaways:");
      summary = parts[0].trim();

      // Extract bullet points
      if (parts.length > 1) {
        String bulletSection = parts[1].trim();
        String[] bullets = bulletSection.split("\n");

        for (String bullet : bullets) {
          String cleaned = bullet.replaceAll("^\\s*[•\\-*]\\s*", "").trim();
          if (!cleaned.isEmpty()) {
            bulletPoints.add(cleaned);
          }
        }
      }
    } else {
      summary = aiResponse.trim();
    }

    // Record the execution in metrics or audit logs
    log.info(
        "Summary generated for user {} in language {} with {} characters",
        context.getUserId(),
        languagePreference,
        summary.length()
    );

    return SummarizeResponse.success(summary, bulletPoints);
  }

  @Override
  protected SummarizeResponse handleError(Exception e, SummarizeRequest request, ExecutionContext context) {
    // Specific error handling based on exception type
    if (e instanceof IllegalArgumentException) {
      log.warn("Validation error for user {}: {}", context.getUserId(), e.getMessage());
      return SummarizeResponse.error(e.getMessage());
    }

    // Log with context information for troubleshooting
    log.error(
        "Error summarizing text for user {} (session: {}): {}",
        context.getUserId(),
        context.getSessionId(),
        e.getMessage(),
        e
    );

    return SummarizeResponse.error("Error summarizing text: " + e.getMessage());
  }

  /**
   * Request record for text summarization.
   */
  public record SummarizeRequest(
      String text,
      int maxLength,
      boolean includeBulletPoints
  ) {

  }

  public record SummarizeResponse(
      boolean success,
      String summary,
      List<String> bulletPoints,
      String errorMessage
  ) {

    /**
     * Factory method for successful response.
     */
    public static SummarizeResponse success(String summary, List<String> bulletPoints) {
      return new SummarizeResponse(true, summary, bulletPoints, null);
    }

    /**
     * Factory method for error response.
     */
    public static SummarizeResponse error(String errorMessage) {
      return new SummarizeResponse(false, null, null, errorMessage);
    }
  }
}