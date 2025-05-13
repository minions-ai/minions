package com.minionslab.core.tool;



import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * Orchestrates tool operations in the Model Context Protocol.
 * Handles tool registration, validation, and execution.
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

