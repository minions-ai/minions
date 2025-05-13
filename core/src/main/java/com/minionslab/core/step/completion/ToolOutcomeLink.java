package com.minionslab.core.step.completion;

import com.minionslab.core.step.StepExecution;
import com.minionslab.core.tool.ToolCall;

public class ToolOutcomeLink implements StepCompletionLink {
    @Override
    public StepCompletionResult check(StepExecution execution) {
        return execution.getToolCalls().stream()
                .anyMatch(this::isStepComplete)
                ? StepCompletionResult.COMPLETE : StepCompletionResult.PASS;
    }
    private boolean isStepComplete(ToolCall toolCall) {
        // Example: check for a step_complete flag in the tool call's response
        if (toolCall.getResponse() != null && toolCall.getResponse().response() != null) {
            String resp = toolCall.getResponse().response().toString();
            return resp.contains("step_complete:true") || resp.contains("step_complete\":true");
        }
        return false;
    }
} 