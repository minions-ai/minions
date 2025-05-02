package com.minionslab.core.tool;

import java.util.Map;

/**
 * Defines the metadata and capabilities of a tool in the Model Context Protocol.
 */
public interface ToolDefinition {
    /**
     * Gets the name of the tool.
     *
     * @return The tool name
     */
    String getName();

    /**
     * Gets the description of what the tool does.
     *
     * @return The tool description
     */
    String getDescription();

    /**
     * Gets the parameter specifications for the tool.
     *
     * @return Map of parameter names to their specifications
     */
    Map<String, Object> getParameters();

    /**
     * Checks if the tool is currently enabled and available for use.
     *
     * @return true if the tool is enabled
     */
    boolean isEnabled();

    /**
     * Gets the version of the tool.
     *
     * @return The tool version
     */
    String getVersion();
} 