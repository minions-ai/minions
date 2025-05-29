package com.minionslab.core.service;

import com.minionslab.core.tool.ToolCall;
import org.springframework.stereotype.Service;

@Service
public class ToolCallService {
    
    public ToolCall call(ToolCall toolCall) {
        return toolCall;
    }
}
