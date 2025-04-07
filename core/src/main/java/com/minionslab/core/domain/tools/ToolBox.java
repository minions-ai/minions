package com.minionslab.core.domain.tools;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface defining the contract for all toolboxes used by the LLMService.
 * A toolbox is a collection of related tools that can be used by the LLM to perform specific tasks.
 */
public interface ToolBox {
    
    /**
     * Get the unique identifier for this toolbox.
     * 
     * @return the toolbox ID
     */
    String getId();
    
    /**
     * Get the name of this toolbox.
     * 
     * @return the toolbox name
     */
    String getName();
    
    /**
     * Get the description of this toolbox.
     * 
     * @return the toolbox description
     */
    String getDescription();
    
    /**
     * Get all tools available in this toolbox.
     * 
     * @return a list of all tools in this toolbox
     */
    List<Tool> getTools();
    
    /**
     * Get a specific tool by its name.
     * 
     * @param toolName the name of the tool to retrieve
     * @return an Optional containing the tool if found, empty otherwise
     */
    Optional<Tool> getToolByName(String toolName);
    
    /**
     * Get the metadata for this toolbox.
     * 
     * @return a map of metadata key-value pairs
     */
    Map<String, Object> getMetadata();
    
    /**
     * Check if this toolbox is enabled.
     * 
     * @return true if the toolbox is enabled, false otherwise
     */
    boolean isEnabled();
    
    /**
     * Get the version of this toolbox.
     * 
     * @return the toolbox version
     */
    String getVersion();
    
    /**
     * Get the category or domain this toolbox belongs to.
     * 
     * @return the toolbox category
     */
    String getCategory();
    
    /**
     * Get the function definitions for all tools in this toolbox.
     * These are used by the LLMService to configure the ChatClient.
     * 
     * @return a map of function names to their parameter definitions
     */
    Map<String, Map<String, Object>> getFunctionDefinitions();
    
    /**
     * Get the required parameters for all functions in this toolbox.
     * 
     * @return an array of required parameter names
     */
    String[] getRequiredParameters();
}
