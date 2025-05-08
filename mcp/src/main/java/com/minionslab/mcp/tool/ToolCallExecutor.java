package com.minionslab.mcp.tool;

import java.util.concurrent.CompletableFuture;

public interface ToolCallExecutor {
    CompletableFuture<MCPToolCall.MCPToolCallResponse> execute();
} 