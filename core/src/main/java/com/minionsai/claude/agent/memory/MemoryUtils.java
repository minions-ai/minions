package com.minionsai.claude.agent.memory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

/**
 * Utility class for memory operations
 */
public class MemoryUtils {

  /**
   * Summarizes chat memory into a concise text
   */
  public static String summarizeChatMemory(ChatMemory chatMemory) {
    // A real implementation would use an LLM to generate a summary
    StringBuilder sb = new StringBuilder();
    sb.append("Conversation summary: ");

    // Get the last few messages
    chatMemory.getMessages().stream()
        .skip(Math.max(0, chatMemory.getMessages().size() - 5))
        .forEach(message -> {
          sb.append(message.getRole()).append(": ");
          sb.append(message.getContent()).append("\n");
        });

    return sb.toString();
  }

  /**
   * Extracts metadata from chat memory
   */
  public static Map<String, Object> extractMetadataFromChatMemory(ChatMemory chatMemory) {
    Map<String, Object> metadata = new HashMap<>();

    // Add message count
    metadata.put("messageCount", chatMemory.getMessages().size());

    // Add timestamp
    metadata.put("timestamp", LocalDateTime.now().toString());

    // Extract entities, topics, etc. using NLP in a real implementation
    return metadata;
  }
}
