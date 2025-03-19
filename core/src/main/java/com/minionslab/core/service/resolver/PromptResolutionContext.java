package com.minionslab.core.service.resolver;

import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Context object containing all information needed for prompt resolution.
 */
@Data
@Accessors(chain = true)
@Builder
public class PromptResolutionContext {

  private MinionType minionType;
  private PromptType promptType;
  private String name;
  private String version;
  private String requestContent;
  private Map<String, Object> metadata;

  // Additional fields for future expansion:
  private String locale;
  private String userId;
  private String tenantId;
  private List<String> capabilities;
} 