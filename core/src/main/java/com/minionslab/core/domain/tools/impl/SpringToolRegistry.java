package com.minionslab.core.domain.tools.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.common.annotation.Toolbox;
import com.minionslab.core.domain.tools.Tool;
import com.minionslab.core.domain.tools.ToolBox;
import com.minionslab.core.domain.tools.ToolRegistry;
import com.minionslab.core.domain.tools.exception.ToolException;
import com.minionslab.core.domain.tools.exception.ToolGroupException;
import com.minionslab.core.domain.tools.exception.ToolInitializationException;
import com.minionslab.core.domain.tools.exception.ToolNotFoundException;
import com.minionslab.core.domain.tools.exception.ToolRegistrationException;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Spring-specific implementation of the ToolRegistry interface.
 * This implementation handles Spring bean discovery and management.
 */
@Service
@Slf4j
public class SpringToolRegistry implements ToolRegistry, ApplicationContextAware {

    private final Map<String, ToolBox> toolBoxes = new ConcurrentHashMap<>();
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext == null) {
            throw new ToolInitializationException("ApplicationContext cannot be null");
        }
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void discoverToolboxes() {
        log.info("Discovering toolboxes...");
        
        // Find all beans with @Toolbox annotation
        Map<String, Object> toolboxBeans = applicationContext.getBeansWithAnnotation(Toolbox.class);
        
        for (Map.Entry<String, Object> entry : toolboxBeans.entrySet()) {
            Object bean = entry.getValue();
            
            if (bean instanceof ToolBox) {
                ToolBox toolbox = (ToolBox) bean;
                registerToolBox(toolbox);
                log.info("Registered toolbox: {}", toolbox.getName());
            } else {
                log.warn("Bean {} is annotated with @Toolbox but does not implement ToolBox interface", entry.getKey());
            }
        }
        
        log.info("Discovered {} toolboxes", toolBoxes.size());
    }

    @Override
    public boolean registerToolBox(ToolBox toolbox) {
        if (toolbox == null || toolbox.getId() == null || toolbox.getId().isEmpty()) {
            log.error("Cannot register toolbox: toolbox is null or has no ID");
            return false;
        }
        
        try {
            toolBoxes.put(toolbox.getId(), toolbox);
            
            // Register all tools from the toolbox
            for (Tool tool : toolbox.getTools()) {
                if (tool != null && tool.getId() != null && !tool.getId().isEmpty()) {
                    tools.put(tool.getId(), tool);
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error registering toolbox: {}", toolbox.getName(), e);
            throw new ToolRegistrationException("Failed to register toolbox: " + toolbox.getName(), e);
        }
    }

    @Override
    public boolean unregisterToolBox(String toolboxId) {
        if (toolboxId == null || toolboxId.isEmpty()) {
            log.error("Cannot unregister toolbox: toolboxId is null or empty");
            return false;
        }
        
        try {
            ToolBox toolbox = toolBoxes.remove(toolboxId);
            if (toolbox == null) {
                log.warn("Toolbox with ID {} not found", toolboxId);
                return false;
            }
            
            // Unregister all tools from the toolbox
            for (Tool tool : toolbox.getTools()) {
                if (tool != null && tool.getId() != null) {
                    tools.remove(tool.getId());
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error unregistering toolbox: {}", toolboxId, e);
            throw new ToolRegistrationException("Failed to unregister toolbox: " + toolboxId, e);
        }
    }

    @Override
    public Optional<ToolBox> getToolBoxById(String toolboxId) {
        return Optional.ofNullable(toolBoxes.get(toolboxId));
    }

    @Override
    public Optional<ToolBox> getToolBoxByName(String toolboxName) {
        return toolBoxes.values().stream()
                .filter(toolbox -> toolbox.getName().equals(toolboxName))
                .findFirst();
    }

    @Override
    public List<ToolBox> getAllToolBoxes() {
        return new ArrayList<>(toolBoxes.values());
    }

    @Override
    public List<ToolBox> getEnabledToolBoxes() {
        return toolBoxes.values().stream()
                .filter(ToolBox::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public List<ToolBox> getToolBoxesByCategory(String category) {
        return toolBoxes.values().stream()
                .filter(toolbox -> toolbox.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Tool> getToolByName(String toolName) {
        return tools.values().stream()
                .filter(tool -> tool.getName().equals(toolName))
                .findFirst();
    }

    @Override
    public List<Tool> getAllTools() {
        return new ArrayList<>(tools.values());
    }

    @Override
    public List<Tool> getEnabledTools() {
        return tools.values().stream()
                .filter(tool -> toolBoxes.values().stream()
                        .filter(ToolBox::isEnabled)
                        .anyMatch(toolbox -> toolbox.getTools().contains(tool)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Tool> getToolsByCategory(String category) {
        return tools.values().stream()
                .filter(tool -> toolBoxes.values().stream()
                        .filter(toolbox -> toolbox.getCategory().equals(category))
                        .anyMatch(toolbox -> toolbox.getTools().contains(tool)))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Map<String, Object>> getAllFunctionDefinitions() {
        Map<String, Map<String, Object>> allFunctionDefinitions = new HashMap<>();
        
        // Collect function definitions from all toolboxes
        for (ToolBox toolbox : toolBoxes.values()) {
            allFunctionDefinitions.putAll(toolbox.getFunctionDefinitions());
        }
        
        return allFunctionDefinitions;
    }
    
    @Override
    public String[] getAllRequiredParameters() {
        Set<String> allRequiredParams = new HashSet<>();
        
        // Collect required parameters from all toolboxes
        for (ToolBox toolbox : toolBoxes.values()) {
            String[] requiredParams = toolbox.getRequiredParameters();
            if (requiredParams != null) {
                for (String param : requiredParams) {
                    allRequiredParams.add(param);
                }
            }
        }
        
        return allRequiredParams.toArray(new String[0]);
    }
} 