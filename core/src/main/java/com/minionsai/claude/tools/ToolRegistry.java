package com.minionsai.claude.tools;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.minionsai.claude.agent.Minion;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;



/**
 * Registry for managing and providing tools to agents
 */
@Service
@Slf4j
public class ToolRegistry implements ApplicationContextAware {

  // Map of tool IDs to tool metadata
  private final Map<String, ToolMetadata> toolMetadataMap = new ConcurrentHashMap<>();
  // Map of agent types to their allowed tools
  private final Map<String, List<String>> agentToolsMap = new ConcurrentHashMap<>();
  // Map of tool groups for easier management
  private final Map<String, List<String>> toolGroupsMap = new ConcurrentHashMap<>();
  private ApplicationContext applicationContext;
  @Autowired
  private FunctionCallbackContext functionCallbackContext;
  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  public void initialize() {
    // Register all tools with @Tool annotation from the application context
    discoverAndRegisterTools();

    // Register default tool groups
    createDefaultToolGroups();
  }

  /**
   * Discovers and registers tools from Spring beans with @Tool annotation
   */
  private void discoverAndRegisterTools() {
    log.info("Discovering tools from application context");

    // Get all beans
    String[] beanNames = applicationContext.getBeanNamesForType(Object.class);

    for (String beanName : beanNames) {
      Object bean = applicationContext.getBean(beanName);
      Class<?> beanClass = bean.getClass();

      // Look for @Tool annotations on methods
      for (Method method : beanClass.getMethods()) {
        Tool toolAnnotation = method.getAnnotation(Tool.class);
        if (toolAnnotation != null) {
          registerTool(beanName, method, toolAnnotation);
        }
      }
    }

    log.info("Discovered {} tools from application context", toolMetadataMap.size());
  }

  /**
   * Registers a tool based on the method and annotation
   */
  private void registerTool(String beanName, Method method, Tool toolAnnotation) {
    String toolId = toolAnnotation.id().isEmpty()
        ? method.getName()
        : toolAnnotation.id();

    String toolName = toolAnnotation.name().isEmpty()
        ? method.getName()
        : toolAnnotation.name();

    String description = toolAnnotation.description();
    String[] categories = toolAnnotation.categories();

    // Create FunctionCallback for this tool
    FunctionCallback functionCallback = functionCallbackContext.registerCallback(
        toolName,
        description,
        applicationContext.getBean(beanName),
        method
    );

    // Create and store tool metadata
    ToolMetadata metadata = ToolMetadata.builder()
        .id(toolId)
        .name(toolName)
        .description(description)
        .beanName(beanName)
        .methodName(method.getName())
        .categories(Arrays.asList(categories))
        .functionCallback(functionCallback)
        .enabled(true)
        .build();

    toolMetadataMap.put(toolId, metadata);

    log.info("Registered tool: {} ({})", toolName, toolId);
  }

  /**
   * Manually register a tool
   */
  public void registerTool(ToolMetadata toolMetadata) {
    toolMetadataMap.put(toolMetadata.getId(), toolMetadata);
    log.info("Manually registered tool: {} ({})",
        toolMetadata.getName(), toolMetadata.getId());
  }

  /**
   * Manually register a tool with FunctionCallback
   */
  public void registerTool(String toolId, String name, String description,
      FunctionCallback callback, String... categories) {
    ToolMetadata metadata = ToolMetadata.builder()
        .id(toolId)
        .name(name)
        .description(description)
        .categories(Arrays.asList(categories))
        .functionCallback(callback)
        .enabled(true)
        .build();

    registerTool(metadata);
  }

  /**
   * Creates default tool groups
   */
  private void createDefaultToolGroups() {
    // Common tool groups
    createToolGroup("basic", "Basic tools for all agents",
        "stringUtils", "mathUtils", "dateTimeUtils");

    createToolGroup("io", "Input/output tools",
        "fileReader", "fileWriter", "httpClient");

    createToolGroup("data", "Data processing tools",
        "jsonProcessor", "csvProcessor", "dataAnalyzer");
  }

  /**
   * Creates a tool group
   */
  public void createToolGroup(String groupId, String description, String... toolIds) {
    List<String> toolList = Arrays.asList(toolIds);
    toolGroupsMap.put(groupId, toolList);

    log.info("Created tool group: {} with {} tools", groupId, toolList.size());
  }

  /**
   * Gets a tool by ID
   */
  public Optional<ToolMetadata> getToolById(String toolId) {
    return Optional.ofNullable(toolMetadataMap.get(toolId));
  }

  /**
   * Gets all registered tools
   */
  public List<ToolMetadata> getAllTools() {
    return new ArrayList<>(toolMetadataMap.values());
  }

  /**
   * Gets all tools in a specific category
   */
  public List<ToolMetadata> getToolsByCategory(String category) {
    return toolMetadataMap.values().stream()
        .filter(tool -> tool.getCategories().contains(category))
        .collect(Collectors.toList());
  }

  /**
   * Gets all tools in a tool group
   */
  public List<ToolMetadata> getToolsInGroup(String groupId) {
    List<String> toolIds = toolGroupsMap.getOrDefault(groupId, Collections.emptyList());

    return toolIds.stream()
        .map(toolMetadataMap::get)
        .filter(tool -> tool != null)
        .collect(Collectors.toList());
  }

  /**
   * Assigns tools to an agent type
   */
  public void assignToolsToAgentType(String agentType, String... toolIds) {
    List<String> toolList = new ArrayList<>();

    // Get existing tools
    List<String> existingTools = agentToolsMap.getOrDefault(agentType, new ArrayList<>());
    toolList.addAll(existingTools);

    // Add new tools
    toolList.addAll(Arrays.asList(toolIds));

    // Remove duplicates
    List<String> uniqueTools = toolList.stream()
        .distinct()
        .collect(Collectors.toList());

    agentToolsMap.put(agentType, uniqueTools);

    log.info("Assigned {} tools to agent type: {}", uniqueTools.size(), agentType);
  }

  /**
   * Assigns all tools in a group to an agent type
   */
  public void assignToolGroupToAgentType(String agentType, String groupId) {
    List<String> groupTools = toolGroupsMap.getOrDefault(groupId, Collections.emptyList());
    assignToolsToAgentType(agentType, groupTools.toArray(new String[0]));

    log.info("Assigned tool group {} to agent type: {}", groupId, agentType);
  }

  /**
   * Gets all tools assigned to an agent type
   */
  public List<ToolMetadata> getToolsForAgentType(String agentType) {
    List<String> toolIds = agentToolsMap.getOrDefault(agentType, Collections.emptyList());

    return toolIds.stream()
        .map(toolMetadataMap::get)
        .filter(tool -> tool != null && tool.isEnabled())
        .collect(Collectors.toList());
  }

  /**
   * Gets FunctionCallbacks for all tools assigned to an agent type
   */
  public FunctionCallback[] getToolsForAgent(String agentType) {
    return getToolsForAgentType(agentType).stream()
        .map(ToolMetadata::getFunctionCallback)
        .toArray(FunctionCallback[]::new);
  }

  /**
   * Enables or disables a tool
   */
  public void setToolEnabled(String toolId, boolean enabled) {
    ToolMetadata tool = toolMetadataMap.get(toolId);
    if (tool != null) {
      tool.setEnabled(enabled);
      log.info("{} tool: {} ({})", enabled ? "Enabled" : "Disabled",
          tool.getName(), tool.getId());
    }
  }

  /**
   * Checks if an agent has access to a specific tool
   */
  public boolean agentHasToolAccess(Minion agent, String toolId) {
    String agentType = agent.getClass().getSimpleName();
    List<String> agentTools = agentToolsMap.getOrDefault(agentType, Collections.emptyList());

    // Check if tool is assigned to this agent type and is enabled
    ToolMetadata tool = toolMetadataMap.get(toolId);
    return tool != null && tool.isEnabled() && agentTools.contains(toolId);
  }

  /**
   * Updates a tool's metadata
   */
  public boolean updateToolMetadata(String toolId, String description, List<String> categories) {
    ToolMetadata tool = toolMetadataMap.get(toolId);
    if (tool == null) {
      return false;
    }

    if (description != null && !description.isEmpty()) {
      tool.setDescription(description);
    }

    if (categories != null && !categories.isEmpty()) {
      tool.setCategories(categories);
    }

    log.info("Updated metadata for tool: {}", toolId);
    return true;
  }
}

