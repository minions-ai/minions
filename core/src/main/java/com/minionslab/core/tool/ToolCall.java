package com.minionslab.core.tool;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.ProcessResult;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Extend ToolCall to add custom fields, metadata, or orchestration logic for tool invocations.</li>
 *   <li>Override methods to support advanced error handling, result aggregation, or request/response management.</li>
 *   <li>Use as the main carrier for tool invocation state and results in the workflow.</li>
 * </ul>
 * <b>Usage:</b> ToolCall represents a single tool invocation, including request, response, and status. Extend for advanced orchestration or tracking.
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCall implements ProcessContext {
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
    
    @Override
    public List getResults() {
        return List.of();
    }
    
    @Override
    public void addResult(ProcessResult result) {
    
    }
    
    public record ToolCallRequest(String input, Map<String,Object> parameters) {
    
    }
    
    public record ToolCallResponse(String response, String error) {
    }


}

