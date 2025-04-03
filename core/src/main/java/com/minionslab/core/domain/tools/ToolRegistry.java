package com.minionslab.core.domain.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.common.annotation.Toolbox;
import com.minionslab.core.domain.tools.exception.ToolException;
import com.minionslab.core.domain.tools.exception.ToolGroupException;
import com.minionslab.core.domain.tools.exception.ToolInitializationException;
import com.minionslab.core.domain.tools.exception.ToolNotFoundException;
import com.minionslab.core.domain.tools.exception.ToolRegistrationException;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Registry for managing and providing tools to agents
 */
@Service @Slf4j public class ToolRegistry implements ApplicationContextAware {

  // Map of tool IDs to tool metadatas
  private final Map<String, List<ToolMetadata>> toolMetadataMap = new ConcurrentHashMap<>();

  // Map of tool groups for easier management
  private final Map<String, List<ToolMetadata>> toolGroupsMap = new ConcurrentHashMap<>();
  private ApplicationContext applicationContext;

  @Autowired private ObjectMapper objectMapper;
  private Map<String, Object> toolboxMap = new ConcurrentHashMap<>();

  @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    if (applicationContext == null) {
      throw new ToolInitializationException("ApplicationContext cannot be null");
    }
    this.applicationContext = applicationContext;
  }

  /**
   * Creates default tool groups
   */
  private void createDefaultToolGroups() {
    try {
      // Common tool groups
      createToolGroup("basic", "Basic tools for all agents", "stringUtils", "mathUtils", "dateTimeUtils");
      createToolGroup("io", "Input/output tools", "fileReader", "fileWriter", "httpClient");
      createToolGroup("data", "Data processing tools", "jsonProcessor", "csvProcessor", "dataAnalyzer");
    } catch (ToolException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to create default tool groups", e);
      throw new ToolInitializationException("Failed to initialize default tool groups", e);
    }
  }

  /**
   * Creates a tool group
   *
   * @throws ToolGroupException if groupId is null or empty, or if toolIds is null or empty
   */
  public void createToolGroup(String groupId, String description, String... toolIds) {
    if (!StringUtils.hasText(groupId)) {
      throw new ToolGroupException(groupId, "Tool group ID cannot be null or empty");
    }
    if (toolIds == null || toolIds.length == 0) {
      throw new ToolGroupException(groupId, "Tool IDs cannot be null or empty");
    }

    // Initialize the group with an empty list if it doesn't exist
    toolGroupsMap.putIfAbsent(groupId, new ArrayList<>());
    log.info("Created tool group: {} with {} tools", groupId, toolIds.length);
  }

  @PostConstruct public void discoverToolboxes() {
    try {
      Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Toolbox.class);
      if (beansWithAnnotation.isEmpty()) {
        log.warn("No beans found with @Toolbox annotation");
        return;
      }

      for (Object bean : beansWithAnnotation.values()) {
        if (bean == null) {
          log.warn("Found null bean with @Toolbox annotation");
          continue;
        }

        Toolbox[] toolboxAnnotations = bean.getClass().getDeclaredAnnotationsByType(Toolbox.class);
        if (toolboxAnnotations == null || toolboxAnnotations.length == 0) {
          log.warn("Bean {} has no @Toolbox annotations", bean.getClass().getName());
          continue;
        }

        for (Toolbox toolboxAnnotation : toolboxAnnotations) {
          registerToolbox(toolboxAnnotation, bean);
        }
      }
    } catch (ToolException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to discover toolboxes", e);
      throw new ToolInitializationException("Failed to initialize toolboxes", e);
    }
  }

  private void registerToolbox(Toolbox toolbox, Object bean) {
    if (toolbox == null) {
      throw new ToolRegistrationException("Toolbox annotation cannot be null");
    }
    if (bean == null) {
      throw new ToolRegistrationException("Bean instance cannot be null");
    }

    try {
      ToolMetadata metadata = createMetadata(toolbox);
      if (metadata == null) {
        throw new ToolRegistrationException(toolbox.name(), new IllegalStateException("Failed to create metadata"));
      }

      // Register metadata in the main map
      List<ToolMetadata> metadataList = toolMetadataMap.computeIfAbsent(metadata.getName(),
          key -> new ArrayList<>());
      metadataList.add(metadata);

      // Store the bean instance with version as key
      String beanKey = metadata.getName() + ":" + metadata.getVersion();
      toolboxMap.put(beanKey, bean);

      // Register in category groups
      for (String category : toolbox.categories()) {
        if (!StringUtils.hasText(category)) {
          log.warn("Skipping empty category for toolbox: {}", metadata.getName());
          continue;
        }
        
        List<ToolMetadata> categoryTools = toolGroupsMap.computeIfAbsent(category, 
            key -> new ArrayList<>());
        categoryTools.add(metadata);
      }

      log.debug("Registered toolbox: {} with version: {} in categories: {}", 
          metadata.getName(), metadata.getVersion(), metadata.getCategories());
    } catch (ToolException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to register toolbox: {}", toolbox.name(), e);
      throw new ToolRegistrationException(toolbox.name(), e);
    }
  }

  private ToolMetadata createMetadata(Toolbox toolboxAnnotation) {
    if (!StringUtils.hasText(toolboxAnnotation.name())) {
      throw new ToolRegistrationException("Toolbox name cannot be null or empty");
    }

    return ToolMetadata.builder()
        .beanName(toolboxAnnotation.name())
        .name(toolboxAnnotation.name())
        .version(toolboxAnnotation.version())
        .categories(List.of(toolboxAnnotation.categories()))
        .build();
  }

  public Object getToolbox(String toolboxName, String version) {
    if (!StringUtils.hasText(toolboxName)) {
      throw new ToolNotFoundException("Toolbox name cannot be null or empty");
    }
    if (!StringUtils.hasText(version)) {
      throw new ToolNotFoundException("Version cannot be null or empty");
    }

    List<ToolMetadata> metadataList = toolMetadataMap.get(toolboxName);
    if (metadataList == null || metadataList.isEmpty()) {
      throw new ToolNotFoundException(toolboxName);
    }

    ToolMetadata metadata = metadataList.stream()
        .filter(toolMetadata -> toolMetadata.getVersion().equals(version))
        .findFirst()
        .orElseThrow(() -> new ToolNotFoundException(toolboxName, version));

    try {
      String beanKey = metadata.getName() + ":" + metadata.getVersion();
      Object bean = toolboxMap.get(beanKey);
      if (bean == null) {
        throw new ToolNotFoundException(toolboxName, version);
      }
      return bean;
    } catch (Exception e) {
      log.error("Failed to get toolbox bean: {} with version: {}", toolboxName, version, e);
      throw new ToolNotFoundException(toolboxName, version);
    }
  }

  public Object getToolbox(String toolboxName) {
    if (!StringUtils.hasText(toolboxName)) {
      throw new ToolNotFoundException("Toolbox name cannot be null or empty");
    }

    List<ToolMetadata> metadataList = toolMetadataMap.get(toolboxName);
    if (metadataList == null || metadataList.isEmpty()) {
      throw new ToolNotFoundException(toolboxName);
    }

    ToolMetadata metadata = metadataList.stream()
        .sorted(Comparator.comparing(ToolMetadata::getVersion).reversed())
        .findFirst()
        .orElseThrow(() -> new ToolNotFoundException(toolboxName));

    try {
      String beanKey = metadata.getName() + ":" + metadata.getVersion();
      Object bean = toolboxMap.get(beanKey);
      if (bean == null) {
        throw new ToolNotFoundException(toolboxName);
      }
      return bean;
    } catch (Exception e) {
      log.error("Failed to get latest version of toolbox: {}", toolboxName, e);
      throw new ToolNotFoundException(toolboxName);
    }
  }
}




