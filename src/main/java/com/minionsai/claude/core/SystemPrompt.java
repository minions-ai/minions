package com.minionsai.claude.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.List;

/**
 * Defines the system prompt that shapes a Minion's behavior,
 * including its goals, role, and context.
 */
@Getter
@Builder
public class SystemPrompt {
  private final String goal;
  private final String role;
  private final String deliverableFormat;
  private final String contextInformation;

  @Singular
  private final List<String> constraints;

  // Generate the actual prompt text for the AI
  public String generatePromptText() {
    StringBuilder prompt = new StringBuilder();
    prompt.append("# Goal\n").append(goal).append("\n\n");
    prompt.append("# Role\n").append(role).append("\n\n");

    if (deliverableFormat != null && !deliverableFormat.isEmpty()) {
      prompt.append("# Deliverable Format\n").append(deliverableFormat).append("\n\n");
    }

    if (contextInformation != null && !contextInformation.isEmpty()) {
      prompt.append("# Context\n").append(contextInformation).append("\n\n");
    }

    if (constraints != null && !constraints.isEmpty()) {
      prompt.append("# Constraints\n");
      for (String constraint : constraints) {
        prompt.append("- ").append(constraint).append("\n");
      }
    }

    return prompt.toString();
  }

  /**
   * Converts to Spring AI SystemMessage for use with ChatClient
   */
  public SystemMessage toSystemMessage() {
    return new SystemMessage(generatePromptText());
  }
}