package com.minionslab.core.tool;


import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.List;
import java.util.Map;

/**
 * Orchestrates tool operations in the Model Context Protocol.
 * Handles tool registration, validation, and execution.
 *
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Implement this interface to provide custom tool registration, lookup, or orchestration logic.</li>
 *   <li>Override methods to support advanced tool validation, parameter management, or execution policies.</li>
 * </ul>
 * <b>Usage:</b> ToolRegistry orchestrates tool operations, registration, and validation. Implement for custom tool management in the framework.
 */
public interface ToolRegistry {
    

    void registerTool(ToolCallback toolCallback);
    

    
    /**
     * Gets the list of available tools.
     *
     * @return List of tool definitions
     */
    List<ToolDefinition> getAvailableTools();
    

    
    /**
     * Checks if a tool is available.
     *
     * @param toolName Name of the tool to check
     * @return true if the tool is available
     */
    boolean isToolAvailable(String toolName);
    
    /**
     * Gets the parameter specification for a tool.
     *
     * @param toolName Name of the tool
     * @return Map of parameter names to their specifications
     */
    Map<String, Object> getToolParameters(String toolName);

    List<ToolCallback> getTools(List<String> requiredTools);
    
    ToolCallback getTool(String toolName);
}

