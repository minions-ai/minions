package com.minionslab.core.tool;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Represents a tool invocation request in the Model Context Protocol.
 */
@Data
@Accessors(chain = true)
@Builder
public class ToolCall {
    private String name;
    private ToolCallRequest request;
    private ToolCallResponse response;
    @Builder.Default
    private ToolCallStatus status = ToolCallStatus.PENDING;
    
    @Override
    public String toString() {
        return "ToolCall{" +
                       "request='" + request + '\'' +
                       ", response='" + response + '\'' +
                       ", status=" + status +
                       '}';
    }
    
    public record ToolCallRequest(String name, String input, Map<String,Object> parameters) {
    
    }
    
    public record ToolCallResponse(String response, String error) {
    }
}

