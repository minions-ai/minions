package com.minionslab.core.domain.tools;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for managing and providing tools to agents.
 * This interface defines the contract for tool registration and retrieval.
 */
public interface ToolRegistry {
    
    /**
     * Registers a toolbox in the registry.
     *
     * @param toolbox The toolbox to register
     * @return true if registration was successful, false otherwise
     */
    boolean registerToolBox(ToolBox toolbox);
    
    /**
     * Unregisters a toolbox from the registry.
     *
     * @param toolboxId The ID of the toolbox to unregister
     * @return true if unregistration was successful, false otherwise
     */
    boolean unregisterToolBox(String toolboxId);
    
    /**
     * Retrieves a toolbox by its ID.
     *
     * @param toolboxId The ID of the toolbox to retrieve
     * @return Optional containing the toolbox if found, empty otherwise
     */
    Optional<ToolBox> getToolBoxById(String toolboxId);
    
    /**
     * Retrieves a toolbox by its name.
     *
     * @param toolboxName The name of the toolbox to retrieve
     * @return Optional containing the toolbox if found, empty otherwise
     */
    Optional<ToolBox> getToolBoxByName(String toolboxName);
    
    /**
     * Retrieves all registered toolboxes.
     *
     * @return List of all toolboxes
     */
    List<ToolBox> getAllToolBoxes();
    
    /**
     * Retrieves all enabled toolboxes.
     *
     * @return List of enabled toolboxes
     */
    List<ToolBox> getEnabledToolBoxes();
    
    /**
     * Retrieves toolboxes by category.
     *
     * @param category The category to filter by
     * @return List of toolboxes in the specified category
     */
    List<ToolBox> getToolBoxesByCategory(String category);
    
    /**
     * Retrieves a tool by its name.
     *
     * @param toolName The name of the tool to retrieve
     * @return Optional containing the tool if found, empty otherwise
     */
    Optional<Tool> getToolByName(String toolName);
    
    /**
     * Retrieves all registered tools.
     *
     * @return List of all tools
     */
    List<Tool> getAllTools();
    
    /**
     * Retrieves all enabled tools.
     *
     * @return List of enabled tools
     */
    List<Tool> getEnabledTools();
    
    /**
     * Retrieves tools by category.
     *
     * @param category The category to filter by
     * @return List of tools in the specified category
     */
    List<Tool> getToolsByCategory(String category);
    
    /**
     * Retrieves all function definitions from all toolboxes.
     *
     * @return Map of function names to their parameter definitions
     */
    Map<String, Map<String, Object>> getAllFunctionDefinitions();
    
    /**
     * Retrieves all required parameters for all functions.
     *
     * @return Array of required parameter names
     */
    String[] getAllRequiredParameters();
}




