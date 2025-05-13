package com.minionslab.core.tool;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.tool.springai.SpringAIToolCallExecutor;
import org.springframework.stereotype.Component;

@Component
public class ToolCallExecutorFactory {
    private final ToolRegistry toolRegistry;
    
    public ToolCallExecutorFactory(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }
    
    public ToolCallExecutor forProvider(String provider, ToolCall call, AgentContext context) {
        if (provider == null || provider.equalsIgnoreCase("spring")) {
            return new SpringAIToolCallExecutor(call, context,toolRegistry);
        }
        throw new IllegalArgumentException("Unknown provider: " + provider);
    }
} 