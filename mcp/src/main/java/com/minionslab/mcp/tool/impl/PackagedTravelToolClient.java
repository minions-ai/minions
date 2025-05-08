package com.minionslab.mcp.tool.impl;

import com.minionslab.mcp.tool.MCPToolCall.MCPToolCallRequest;
import com.minionslab.mcp.tool.MCPToolCall.MCPToolCallResponse;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.ai.tool.metadata.DefaultToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.util.ReflectionUtils;

import java.util.List;

/**
 * Dummy tool call client that returns a static list of city destinations.
 */
public class PackagedTravelToolClient {
    
    private final static String inputSchema = """
            {
              "type": "object",
              "properties": {
                "packageType": {
                  "type": "string",
                  "description": "The type of travel package (e.g., 'all-inclusive', 'adventure', 'family', 'honeymoon', 'cruise', etc.)."
                },
                "destinationQuery": {
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
                "numberOfTravelers": {
                  "type": "integer",
                  "description": "The number of travelers in the package."
                },
                "budget": {
                  "type": "number",
                  "description": "Optional budget for the trip in USD."
                }
              },
              "required": ["packageType", "destinationQuery", "startDate", "endDate", "numberOfTravelers"]
            }
            """;
    
    public static MethodToolCallback getcallback() {
        PackagedTravelToolClient dummy = new PackagedTravelToolClient();
        
        return new MethodToolCallback(new DefaultToolDefinition(
                "packagedTravelTool",
                "This tool searches all the packaged travel destinations available for travel in the set time frame",
                inputSchema
        ), new DefaultToolMetadata(false), ReflectionUtils.findMethod(PackagedTravelToolClient.class, "call", MCPToolCallRequest.class), dummy,
                new DefaultToolCallResultConverter());
    }
    
    /**
     * Simulates a tool call that returns a list of city destinations.
     *
     * @param request The tool call request (ignored in this dummy implementation)
     * @return An MCPToolCallResponse containing a static list of city names
     */
    public MCPToolCallResponse call(MCPToolCallRequest request) {
        List<String> cities = List.of("Paris", "London", "New York", "Tokyo", "Sydney");
        String response = String.join(", ", cities);
        return new MCPToolCallResponse(response, null);
    }
} 