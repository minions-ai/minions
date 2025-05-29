package com.minionslab.core.tool;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.ProcessResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ToolCallContext implements ProcessContext {
    private final ToolCall toolCall;
    private ProcessResult result;
    
    public ToolCallContext(ToolCall toolCall) {
        this.toolCall = toolCall;
    }
    
    
    @Override
    public List getResults() {
        return List.of();
    }
    
    @Override
    public void addResult(ProcessResult result) {
    
    }


}