package com.minionslab.core.tool;

import java.util.Map;

/**
 * Functional interface for executing tool operations in the Model Context Protocol.
 */
@FunctionalInterface
public interface ToolExecutor {
    /**
     * Executes the tool with the given parameters.
     *
     * @param parameters The parameters for the tool execution
     * @return The result of the tool execution
     * @throws Exception if the tool execution fails
     */
    Object execute(Map<String, Object> parameters) throws Exception;
}
