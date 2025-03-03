package com.minionsai.claude.capability;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a match between a task and an agent that can handle it
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinionMatch {

  private String agentType;
  private String capabilityName;
  private Double score;
  private String matchedCapabilityDescription;
  private Map<String, Object> additionalMetadata;
}
