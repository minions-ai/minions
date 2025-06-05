package com.minionslab.core.step.processor;

import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.service.ToolCallService;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.tool.ToolCall;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolCallStepProcessor implements StepProcessor {
    private ToolCallService toolCallService;
    
    public ToolCallStepProcessor(ToolCallService toolCallService) {
        this.toolCallService = toolCallService;
    }
    
    @Override
    public boolean accepts(StepContext input) {
        return input.getToolCalls() != null && !input.getToolCalls().isEmpty() && !input.unfinishedToolCalls().isEmpty();
    }
    
    @Override
    public StepContext process(StepContext input) {
        List<ToolCall> toolCalls = input.getToolCalls();
        for (ToolCall toolCall : toolCalls) {
            toolCallService.call(toolCall);
        }
        return input;
    }
}
