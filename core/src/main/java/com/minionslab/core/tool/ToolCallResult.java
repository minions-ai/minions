package com.minionslab.core.tool;

/**
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Extend ToolCallResult to add custom fields, metadata, or result handling logic for tool invocations.</li>
 *   <li>Override methods to support advanced response extraction or error handling.</li>
 * </ul>
 * <b>Usage:</b> ToolCallResult represents the output of a tool call, including the response. Extend for advanced orchestration or tracking.
 */
public class ToolCallResult  {
    private final ToolCall.ToolCallResponse toolCallResponse;

    public ToolCallResult(ToolCall.ToolCallResponse toolCallResponse) {
        this.toolCallResponse = toolCallResponse;
    }

    public ToolCall.ToolCallResponse getToolCallResponse() { return toolCallResponse; }
} 