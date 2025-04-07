package com.minionslab.core.domain.tools;

import java.util.Map;
import java.util.function.Function;

/**
 * Interface defining the contract for all tools used by the LLMService.
 * A tool is a specific capability that can be invoked by the LLM to perform a task.
 */
public interface Tool {
    
    /**
     * Get the unique identifier for this tool.
     * 
     * @return the tool ID
     */
    String getId();
    
    /**
     * Get the name of this tool.
     * 
     * @return the tool name
     */
    String getName();
    
    /**
     * Get the description of this tool.
     * 
     * @return the tool description
     */
    String getDescription();
    
    /**
     * Get the parameters for this tool.
     * 
     * @return a map of parameter names to parameter definitions
     */
    Map<String, Object> getParameters();
    
    /**
     * Execute this tool with the given parameters.
     * 
     * @param parameters the parameters to use when executing the tool
     * @return the result of executing the tool
     */
    Object execute(Map<String, Object> parameters);
    
    /**
     * Get the metadata for this tool.
     * 
     * @return a map of metadata key-value pairs
     */
    Map<String, Object> getMetadata();
    
    /**
     * Check if this tool is enabled.
     * 
     * @return true if the tool is enabled, false otherwise
     */
    boolean isEnabled();
    
    /**
     * Get the version of this tool.
     * 
     * @return the tool version
     */
    String getVersion();
    
    /**
     * Get the category or domain this tool belongs to.
     * 
     * @return the tool category
     */
    String getCategory();

    Function getFunction();
}