package com.minionslab.mcp.step;

import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.tool.MCPToolCall;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a single call execution (either model or tool) in the execution chain.
 * This class tracks the execution details of either a model call or tool call.
 */
@Data
@Accessors(chain = true)
public class CallExecution {
    private final String id = UUID.randomUUID().toString();
    private final CallType type;
    private final Object call; // MCPModelCall or MCPToolCall
    private final Instant timestamp = Instant.now();
    private Map<String, Object> metadata = new HashMap<>();
    
    public CallExecution(CallType type, Object call) {
        this.type = type;
        this.call = call;
    }
    
    /**
     * Gets the model call if this execution is of type MODEL.
     *
     * @return The model call or null if this is not a model call execution
     */
    public MCPModelCall getModelCall() {
        return type == CallType.MODEL ? (MCPModelCall) call : null;
    }
    
    /**
     * Gets the tool call if this execution is of type TOOL.
     *
     * @return The tool call or null if this is not a tool call execution
     */
    public MCPToolCall getToolCall() {
        return type == CallType.TOOL ? (MCPToolCall) call : null;
    }
    
    /**
     * Adds metadata to this call execution.
     *
     * @param key The metadata key
     * @param value The metadata value
     * @return This call execution instance for chaining
     */
    public CallExecution addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
} 