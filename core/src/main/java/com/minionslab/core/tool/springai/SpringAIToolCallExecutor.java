package com.minionslab.core.tool.springai;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.memory.ModelMemory;
import com.minionslab.core.tool.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Executes a tool call using the Spring AI Tool infrastructure, with robust error handling.
 * This class is responsible for invoking the correct tool callback, handling all edge cases,
 * and updating the tool call status and response accordingly.
 */
@Slf4j
public class SpringAIToolCallExecutor extends AbstractToolCallExecutor<String> {
    @Getter
    private final ToolCall toolCall;
    private final AgentContext context;
    private final ToolRegistry toolRegistry;
    private final Executor executor;
    private final ModelMemory modelMemory;

    public SpringAIToolCallExecutor(ToolCall toolCall, AgentContext context, ToolRegistry toolRegistry) {
        this(toolCall, context, toolRegistry, ForkJoinPool.commonPool());
    }

    public SpringAIToolCallExecutor(ToolCall toolCall,
                                    AgentContext context, ToolRegistry toolRegistry,
                                    Executor executor) {
        super(toolCall, context);
        this.toolCall = toolCall;
        this.context = context;
        this.toolRegistry = toolRegistry;
        this.executor = executor;
        this.modelMemory = context.getChatMemory();
    }

    @Override
    protected String callTool(ToolCall toolCall) {
        // Lookup tool callback
        ToolCallback toolCallback = toolRegistry.getTool(toolCall.getName());
        if (toolCallback == null) {
            throw new IllegalArgumentException("Tool not found: " + toolCall.getName());
        }
        ToolContext toolContext = new ToolContext(toolCall.getRequest().parameters());
        return toolCallback.call(toolCall.getRequest().input(), toolContext);
    }

    @Override
    protected ToolCall.ToolCallResponse toToolCallResponse(String rawResponse) {
        return new ToolCall.ToolCallResponse(rawResponse, null);
    }

    @Override
    protected Executor getExecutor() {
        return executor;
    }

    @Override
    protected void handleProviderResponse(String rawResponse) {
        // Optionally save or log the raw response
    }
} 