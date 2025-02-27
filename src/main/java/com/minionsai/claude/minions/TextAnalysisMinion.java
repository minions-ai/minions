package com.minionsai.claude.minions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionsai.claude.core.*;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.*;

@Slf4j
public class TextAnalysisMinion extends Minion {
  private final ChatClient chatClient;

  public TextAnalysisMinion(String id, String name, SystemPrompt systemPrompt, ChatClient chatClient) {
    super(id, name, "text-analysis", systemPrompt);
    this.chatClient = chatClient;

    // Add default tools
    addTool(new SentimentAnalysisTool(chatClient));
    addTool(new KeywordExtractionTool(chatClient));
    addTool(new TextCategorizationTool(chatClient));
  }

  @Override
  public boolean canHandleTask(Task task) {
    String taskType = task.getType();
    return taskType != null && (
        taskType.equals("analyze-sentiment") ||
            taskType.equals("extract-keywords") ||
            taskType.equals("categorize-text") ||
            taskType.equals("summarize-text")
    );
  }

  @Override
  public StructuredOutput processTask(Task task) {
    String taskType = task.getType();
    StructuredOutput.Builder outputBuilder = StructuredOutput.builder()
        .taskId(task.getId());

    try {
      switch (taskType) {
        case "analyze-sentiment":
          processSentimentTask(task, outputBuilder);
          break;
        case "extract-keywords":
          processKeywordExtractionTask(task, outputBuilder);
          break;
        case "categorize-text":
          processCategorizationTask(task, outputBuilder);
          break;
        case "summarize-text":
          processSummarizationTask(task, outputBuilder);
          break;
        default:
          outputBuilder.success(false)
              .message("Unknown task type: " + taskType);
      }
    } catch (Exception e) {
      log.error("Error processing task: {}", e.getMessage(), e);
      outputBuilder.success(false)
          .message("Error processing task: " + e.getMessage());
    }

    return outputBuilder.build();
  }

  private void processSentimentTask(Task task, StructuredOutput.Builder outputBuilder) {
    String text = (String) task.getParameter("text");
    if (text == null || text.isEmpty()) {
      outputBuilder.success(false).message("No text provided for sentiment analysis");
      return;
    }

    try {
      // Try using the tool first
      ToolResult result = executeTool("sentiment-analysis", Map.of("text", text));

      outputBuilder.dataEntry("sentiment", result.getData())
          .toolExecution(ToolExecution.builder()
              .toolId("sentiment-analysis")
              .parameters(Map.of("text", text))
              .result(result.getData())
              .build());

    } catch (Exception e) {
      log.warn("Tool execution failed, falling back to LLM: {}", e.getMessage());

      // Fall back to Spring AI ChatClient
      List<Message> messages = new ArrayList<>();
      messages.add(getSystemPrompt().toSystemMessage());

      String userPrompt = String.format(
          "Analyze the sentiment of the following text. " +
              "Return a JSON object with 'sentiment' (positive, negative, neutral) " +
              "and 'confidence' (0-1 score):\n\n%s",
          text
      );

      messages.add(new UserMessage(userPrompt));

      String response = chatClient.call(new Prompt(messages)).getResult().getOutput().getContent();

      try {
        // Parse the JSON response - simplified example
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> sentimentData = objectMapper.readValue(response, Map.class);
        outputBuilder.dataEntry("sentiment", sentimentData);
      } catch (Exception jsonException) {
        log.error("Failed to parse JSON response: {}", jsonException.getMessage());
        outputBuilder.dataEntry("rawResponse", response)
            .message("Failed to parse structured sentiment data");
      }
    }
  }

  // Implementation placeholders for other methods
  private void processKeywordExtractionTask(Task task, StructuredOutput.Builder outputBuilder) {
    // Implementation using Spring AI
  }

  private void processCategorizationTask(Task task, StructuredOutput.Builder outputBuilder) {
    // Implementation using Spring AI
  }

  private void processSummarizationTask(Task task, StructuredOutput.Builder outputBuilder) {
    // Implementation using Spring AI
  }
}