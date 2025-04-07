package com.minionslab.core.domain.tools.impl;

import com.minionslab.core.domain.tools.Tool;
import com.minionslab.core.domain.tools.ToolBox;
import com.minionslab.core.domain.tools.ToolRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the ToolRegistry interface.
 */
//@Service
public class DefaultToolRegistry implements ToolRegistry {
    
    private final Map<String, ToolBox> toolBoxes = new ConcurrentHashMap<>();
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();
    
    @Override
    public boolean registerToolBox(ToolBox toolbox) {
        if (toolbox == null || toolbox.getId() == null || toolbox.getId().isEmpty()) {
            return false;
        }
        
        toolBoxes.put(toolbox.getId(), toolbox);
        
        // Register all tools from the toolbox
        for (Tool tool : toolbox.getTools()) {
            if (tool != null && tool.getId() != null && !tool.getId().isEmpty()) {
                tools.put(tool.getId(), tool);
            }
        }
        
        return true;
    }
    
    @Override
    public boolean unregisterToolBox(String toolboxId) {
        if (toolboxId == null || toolboxId.isEmpty()) {
            return false;
        }
        
        ToolBox toolbox = toolBoxes.remove(toolboxId);
        if (toolbox == null) {
            return false;
        }
        
        // Unregister all tools from the toolbox
        for (Tool tool : toolbox.getTools()) {
            if (tool != null && tool.getId() != null) {
                tools.remove(tool.getId());
            }
        }
        
        return true;
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