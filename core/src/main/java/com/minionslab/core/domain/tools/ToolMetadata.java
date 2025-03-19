package com.minionslab.core.domain.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata for a registered tool
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolMetadata {

  private String id;
  private String name;
  private String version;
  private String beanName;
  private List<String> categories;
  private boolean enabled;

  /**
   * Additional metadata for the tool
   */
  @Builder.Default
  private Map<String, Object> metadata = new HashMap<>();
}
