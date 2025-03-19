package com.minionslab.core.domain;

import com.minionslab.core.common.exception.MinionCreationException;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.tools.ToolRegistry;
import com.minionslab.core.service.MinionLifecycleManager;
import com.minionslab.core.service.resolver.PromptResolver;
import com.minionslab.core.service.resolver.PromptResolverChainFactory;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating Minion instances based on their minionType and configuration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinionFactory {

  private final ChatMemory chatMemory;
  private final MinionLifecycleManager lifecycleManager;
  private final MinionRegistry minionRegistry;
  private final ToolRegistry toolRegistry;
  private final PromptResolverChainFactory resolverChainFactory;
  private final PromptResolver promptResolver;



  /**
   * Creates a new Minion instance with the provided system prompt.
   *
   * @param type         The minionType of Minion to create
   * @param minionPrompt The system prompt to use
   * @return A new Minion instance
   */
  private Minion createMinionWithPrompt(MinionType type, MinionPrompt minionPrompt) {
    Minion minion = Minion.builder()
        .minionType(type)
        .minionPrompt(minionPrompt)
        .chatMemory(chatMemory)
        .build();

    // Register tools for the minion
    registerToolsForMinion(minion);

    return minion;
  }

  /**
   * Registers tools for a minion based on its minionType and configuration.
   *
   * @param minion The minion to register tools for
   */
  private void registerToolsForMinion(Minion minion) {
    try {
      // Get available tools for the minion minionType
      String[] toolboxNames = getToolboxNamesForType(minion.getMinionType());

      // Register each toolbox
      for (String toolboxName : toolboxNames) {
        Object toolbox = toolRegistry.getToolbox(toolboxName);
        if (toolbox != null) {
          minion.getToolboxes().put(toolboxName, toolbox);
          minion.getToolboxNames().add(toolboxName);
        }
      }
    } catch (Exception e) {
      log.error("Failed to register tools for minion: {}", minion.getMinionId(), e);
      throw new MinionCreationException("Failed to register tools for minion", e);
    }
  }

  /**
   * Gets the toolbox names for a specific minion minionType.
   *
   * @param type The minion minionType
   * @return Array of toolbox names
   */
  private String[] getToolboxNamesForType(MinionType type) {
    return switch (type) {
      case WORKFLOW_MANAGER -> new String[]{"workflow", "task"};
      case COMMUNICATION_AGENT -> new String[]{"communication", "message"};
      case SECURITY_MONITOR -> new String[]{"security", "monitoring"};
      case RESOURCE_MANAGER -> new String[]{"resource", "management"};
      case USER_SUPPORT -> new String[]{"support", "user"};
      case AUTOMATION_ENGINEER -> new String[]{"automation", "engineering"};
      case INTEGRATION_AGENT -> new String[]{"integration", "api"};
      case TESTING_AGENT -> new String[]{"testing", "validation"};
      case LOGGING_AGENT -> new String[]{"logging", "monitoring"};
      case USER_DEFINED_AGENT -> new String[]{"basic"};
    };
  }

  public Minion createMinion(MinionType minionType) {
    Minion minion = Minion.builder()
        .minionType(minionType)
        .build();

    return minion;
  }

  public Minion createMinion(MinionType minionType, Map<String, String> metadata) {
    return null;
  }

  public Minion createMinion(MinionType minionType, Map<String, String> metadata, MinionPrompt prompt) {
    Minion minion = Minion.builder()
        .minionType(minionType)
        .minionPrompt(prompt)
        .chatMemory(chatMemory)
        .metadata(metadata)
        .build();

    // Register tools for the minion
    registerToolsForMinion(minion);
    return minion;
  }
}