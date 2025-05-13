package com.minionslab.core.tool;

import java.util.concurrent.CompletableFuture;

public interface ToolCallExecutor<T> {
    CompletableFuture<ToolCall.ToolCallResponse> executeAsync();
    
    ToolCall.ToolCallResponse execute();
}