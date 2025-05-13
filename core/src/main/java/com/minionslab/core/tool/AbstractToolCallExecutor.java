package com.minionslab.core.tool;

import com.minionslab.core.context.AgentContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Generic abstract tool call executor for any provider response type T.
 * Subclasses specify the provider response type and implement conversion logic.
 */
@Slf4j
public abstract class AbstractToolCallExecutor<T> implements ToolCallExecutor<T> {
    protected final ToolCall toolCall;
    protected final AgentContext context;

    public AbstractToolCallExecutor(ToolCall toolCall, AgentContext context) {
        this.toolCall = toolCall;
        this.context = context;
    }

    @Override
    public CompletableFuture<ToolCall.ToolCallResponse> executeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            return execute();
        }, getExecutor());
    }
    
    @Override
    public ToolCall.ToolCallResponse execute() {
        try {
            validateToolCall();
            toolCall.setStatus(ToolCallStatus.EXECUTING);
            T rawResponse = callTool(toolCall);
            handleProviderResponse(rawResponse);
            ToolCall.ToolCallResponse response = toToolCallResponse(rawResponse);
            toolCall.setResponse(response);
            toolCall.setStatus(ToolCallStatus.COMPLETED);
            log.info("Tool call execution completed successfully: {}", toolCall.getName());
            return response;
        } catch (Exception e) {
            return failToolCall(e.getMessage(), e);
        }
    }
    
    protected abstract T callTool(ToolCall toolCall);
    protected abstract ToolCall.ToolCallResponse toToolCallResponse(T rawResponse);
    protected abstract Executor getExecutor();
    protected void handleProviderResponse(T rawResponse) {}

    protected void validateToolCall() {
        if (toolCall.getName() == null || toolCall.getName().isEmpty()) {
            throw new IllegalArgumentException("Tool name is null or empty");
        }
        if (toolCall.getRequest() == null) {
            throw new IllegalArgumentException("Tool call request is null");
        }
    }

    protected ToolCall.ToolCallResponse failToolCall(String error, Exception e) {
        log.error("Tool call failed: {}", error, e);
        toolCall.setStatus(ToolCallStatus.FAILED);
        ToolCall.ToolCallResponse response = new ToolCall.ToolCallResponse(null, error);
        toolCall.setResponse(response);
        return response;
    }
} 