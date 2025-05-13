package com.minionslab.core.tool.impl;

import com.minionslab.core.tool.ToolCall.ToolCallRequest;
import com.minionslab.core.tool.ToolCall.ToolCallResponse;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.ai.tool.metadata.DefaultToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.util.ReflectionUtils;

import java.util.List;

/**
 * Dummy tool call client that returns a static list of city destinations.
 */
public class DestinationSearchTool {
    
    private final static String inputSchema = """
            {
              "type": "object",
              "properties": {
                "query": {
                  "type": "string",
                  "description": "A search query or keywords for the desired destination(s), e.g. 'Europe', 'beach', 'mountains', or a city name."
                },
                "startDate": {
                  "type": "string",
                  "format": "date",
                  "description": "The start date for the travel period (YYYY-MM-DD)."
                },
                "endDate": {
                  "type": "string",
                  "format": "date",
                  "description": "The end date for the travel period (YYYY-MM-DD)."
                },
                "budget": {
                  "type": "number",
                  "description": "Optional budget for the trip in USD."
                }
              },
              "required": ["query", "startDate", "endDate"]
            }
            """;
    
    public static MethodToolCallback getcallback() {
        DestinationSearchTool dummy = new DestinationSearchTool();
        
        return new MethodToolCallback(new DefaultToolDefinition(
                "destinationSearchTool",
                "This tool searches all the destinations available for travel in the set time frame",
                inputSchema
        ), new DefaultToolMetadata(false), ReflectionUtils.findMethod(DestinationSearchTool.class, "call", ToolCallRequest.class), dummy,
                new DefaultToolCallResultConverter());
    }
    
    /**
     * Simulates a tool call that returns a list of city destinations.
     *
     * @param request The tool call request (ignored in this dummy implementation)
     * @return An ToolCallResponse containing a static list of city names
     */
    public ToolCallResponse call(ToolCallRequest request) {
        List<String> cities = List.of("Paris", "London", "New York", "Tokyo", "Sydney");
        String response = String.join(", ", cities);
        return new ToolCallResponse(response, null);
    }
} 