package com.minionslab.core.domain.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.common.annotation.Toolbox;
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

/**
 * Registry for managing and providing tools to agents
 */
@Service @Slf4j public class ToolRegistry implements ApplicationContextAware {

  // Map of tool IDs to tool metadata
  private final Map<String, List<ToolMetadata>> toolMetadataMap = new ConcurrentHashMap<>();

  // Map of tool groups for easier management
  private final Map<String, List<String>> toolGroupsMap = new ConcurrentHashMap<>();
  private ApplicationContext applicationContext;

  @Autowired private ObjectMapper objectMapper;
  private Map<Toolbox, Object> toolboxMap = new ConcurrentHashMap<>();

  @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }


  /**
   * Creates default tool groups
   */
  private void createDefaultToolGroups() {
    // Common tool groups
    createToolGroup("basic", "Basic tools for all agents", "stringUtils", "mathUtils", "dateTimeUtils");

    createToolGroup("io", "Input/output tools", "fileReader", "fileWriter", "httpClient");

    createToolGroup("data", "Data processing tools", "jsonProcessor", "csvProcessor", "dataAnalyzer");
  }

  /**
   * Creates a tool group
   */
  public void createToolGroup(String groupId, String description, String... toolIds) {
    List<String> toolList = Arrays.asList(toolIds);
    toolGroupsMap.put(groupId, toolList);
    log.info("Created tool group: {} with {} tools", groupId, toolList.size());
  }


  @PostConstruct public void discoverToolboxes() {
    Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Toolbox.class);
    for (Object bean : beansWithAnnotation.values()) {
      Toolbox[] toolboxAnnotations = bean.getClass().getDeclaredAnnotationsByType(Toolbox.class);
      for (Toolbox toolboxAnnotation : toolboxAnnotations) {
        registerToolbox(toolboxAnnotation);
      }
    }
  }

  private void registerToolbox(Toolbox toolbox) {
    ToolMetadata metadata = createMetadata(toolbox);
    List<ToolMetadata> metadataList = toolMetadataMap.computeIfAbsent(metadata.getName(), (key) -> {
      return new ArrayList<>();
    });
    metadataList.add(metadata);
  }

  private ToolMetadata createMetadata(Toolbox toolboxAnnotation) {
    return ToolMetadata.builder().beanName(toolboxAnnotation.name()).name(toolboxAnnotation.name()).version(toolboxAnnotation.version())
        .categories(List.of(toolboxAnnotation.categories())).build();
  }

  public Object getToolbox(String toolboxName, String version) {
    ToolMetadata metadata = toolMetadataMap.get(toolboxName).stream().filter(toolMetadata -> toolMetadata.getVersion().equals(version))
        .findFirst().orElseThrow();
    return applicationContext.getBean(metadata.getBeanName());
  }

  public Object getToolbox(String toolboxName) {
    ToolMetadata metadata = toolMetadataMap.get(toolboxName).stream().sorted(Comparator.comparing(ToolMetadata::getVersion).reversed())
        .findFirst().orElseThrow();
    return applicationContext.getBean(metadata.getBeanName());
  }
}




