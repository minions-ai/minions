package com.minionsai.claude.agent.memory;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;

/**
 * Represents a memory stored by an agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinionMemory {

  private String id;
  private String agentId;
  private String agentType;
  private ChatMemory content;
  private MemoryType type;
  private LocalDateTime createdAt;
  private LocalDateTime expiresAt;
  private double importance;
  private Map<String, Object> metadata;
}
