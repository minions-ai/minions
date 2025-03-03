package com.minionsai.claude.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.model.function.FunctionCallback; /**
 * Metadata for a registered tool
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolMetadata {

  private String id;
  private String name;
  private String description;
  private String beanName;
  private String methodName;
  private List<String> categories;
  private FunctionCallback functionCallback;
  private boolean enabled;

  /**
   * Additional metadata for the tool
   */
  @Builder.Default
  private Map<String, Object> metadata = new HashMap<>();
}
