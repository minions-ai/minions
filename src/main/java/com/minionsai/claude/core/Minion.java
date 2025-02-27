package com.minionsai.claude.core;

import com.minionsai.claude.exceptions.ToolExecutionException;
import com.minionsai.claude.exceptions.ToolNotFoundException;
import com.minionsai.claude.tools.Tool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;

import java.util.*;

/**
 * Core Minion abstract class that defines the fundamental capabilities
 * of all Minions in the framework.
 */
@Slf4j
@Getter
public abstract class Minion {
  private final String id;
  private final String name;
  private final String specialization;
  private final SystemPrompt systemPrompt;
  private final Set<Tool> availableTools;

  protected Minion(String id, String name, String specialization, SystemPrompt systemPrompt) {
    this.id = id;
    this.name = name;
    this.specialization = specialization;
    this.systemPrompt = systemPrompt;
    this.availableTools = new HashSet<>();
  }

  // Core functionality that all Minions must implement
  public abstract StructuredOutput processTask(Task task);

  // Capability check
  public abstract boolean canHandleTask(Task task);

  // Tool management
  public void addTool(Tool tool) {
    this.availableTools.add(tool);
  }

  protected Optional<Tool> getTool(String toolId) {
    return availableTools.stream()
        .filter(tool -> tool.getId().equals(toolId))
        .findFirst();
  }

  protected ToolResult executeTool(String toolId, Map<String, Object> params)
      throws ToolNotFoundException, ToolExecutionException {
    Tool tool = getTool(toolId)
        .orElseThrow(() -> new ToolNotFoundException(toolId));

    return tool.execute(params);
  }

  public abstract void finalizeMinion();
}