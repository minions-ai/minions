// File: tools/ToolRepository.java
package com.example.multiagent.tools;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class ToolRepository {

  // In-memory cache for available tools.
  private List<Tool> availableTools = new ArrayList<>();

  public ToolRepository() {
    // Optionally load default tools here.
  }

  public List<Tool> getAvailableTools() {
    return availableTools;
  }

  public void addTool(Tool tool) {
    availableTools.add(tool);
  }
}
