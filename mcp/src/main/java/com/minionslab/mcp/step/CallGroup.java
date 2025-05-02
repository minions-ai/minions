package com.minionslab.mcp.step;

import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.model.ModelCallStatus;
import com.minionslab.mcp.tool.MCPToolCall;
import com.minionslab.mcp.tool.ToolCallStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Represents a group of related calls in a step execution.
 * A call group typically starts with a model call that may generate tool calls,
 * followed by a follow-up model call to process tool results.
 */
@Data
@Accessors(chain = true)
public class CallGroup {
    private final String id = UUID.randomUUID().toString();
    private final MCPModelCall initiatingModelCall;
    private final List<MCPToolCall> toolCalls = new ArrayList<>();
    private MCPModelCall followUpModelCall;
    private CallGroupStatus status = CallGroupStatus.PENDING;
    private final Instant createdAt = Instant.now();
    private Instant completedAt;
    
    public CallGroup(MCPModelCall initialCall) {
        this.initiatingModelCall = initialCall;
    }
    
    public void addToolCall(MCPToolCall toolCall) {
        this.toolCalls.add(toolCall);
    }
    
    public boolean isComplete() {
        if (toolCalls.isEmpty()) {
            return initiatingModelCall.getStatus() == ModelCallStatus.COMPLETED;
        }
        
        boolean toolCallsComplete = toolCalls.stream()
                .allMatch(call -> call.getStatus() == ToolCallStatus.COMPLETED);
                
        return toolCallsComplete && 
               (followUpModelCall == null || followUpModelCall.getStatus() == ModelCallStatus.COMPLETED);
    }
    
    public void addCall(CallExecution callExecution) {
        if (callExecution.getType() == CallType.MODEL) {
            MCPModelCall modelCall = callExecution.getModelCall();
            if (followUpModelCall == null) {
                followUpModelCall = modelCall;
                status = CallGroupStatus.EXECUTING_FOLLOW_UP_MODEL_CALL;
            }
        } else if (callExecution.getType() == CallType.TOOL) {
            MCPToolCall toolCall = callExecution.getToolCall();
            toolCalls.add(toolCall);
            status = CallGroupStatus.EXECUTING_TOOL_CALLS;
        }
    }
    
    public Collection<MCPModelCall> getModelCalls() {
        return List.of(initiatingModelCall, followUpModelCall);
    }
}