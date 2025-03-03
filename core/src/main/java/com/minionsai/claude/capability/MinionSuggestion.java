package com.minionsai.claude.capability;

import lombok.Builder;
import lombok.Data;

/**
 * A suggestion of an agent for a task
 */
@Data
@Builder
public class MinionSuggestion {

  private String agentType;
  private String capabilityName;
  private Double matchScore;
  private String existingAgentId;
  private String capabilityDescription;
}
