package com.minionslab.mcp.tool;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Represents a tool invocation request in the Model Context Protocol.
 */
@Data
@Accessors(chain = true)
@Builder
public class MCPToolCall {
    private String name;
    private MCPToolCallRequest request;
    private MCPToolCallResponse response;
    private ToolCallStatus status = ToolCallStatus.PENDING;
    
    @Override
    public String toString() {
        return "MCPToolCall{" +
                       "request='" + request + '\'' +
                       ", response='" + response + '\'' +
                       ", status=" + status +
                       '}';
    }
    
    public record MCPToolCallRequest(String name, String parameters, String explanation) {
    }
    
    public record MCPToolCallResponse(String response, String error) {
    }
}

