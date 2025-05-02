package com.minionslab.mcp.model;

import com.minionslab.mcp.config.ModelConfig;
import com.minionslab.mcp.message.MCPMessage;
import com.minionslab.mcp.tool.MCPToolCall;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a model invocation request in the Model Context Protocol.
 */
@Data
@Accessors(chain = true)
public class MCPModelCall {
    private final MCPModelCallRequest request;
    private ModelCallStatus status;
    private MCPModelCallResponse response;
    private MCPModelCallError error;
    private List<MCPToolCall> toolCalls = new ArrayList<>();
    
    
    public MCPModelCall(MCPModelCallRequest request) {
        this.request = request;
        this.status = ModelCallStatus.PENDING;
    }
    
    
    @Override
    public String toString() {
        return "MCPModelCall{" +
                       "request=" + request +
                       ", response=" + response +
                       ", status=" + status +
                       '}';
    }
    
    
    public record MCPModelCallRequest(List<MCPMessage> messages, Map<String, Object> parameters) {
    }
    
    public record MCPModelCallResponse(List<MCPMessage> messages) {

    }
    
    public record MCPModelCallError(MCPMessage error) {
    }
}

