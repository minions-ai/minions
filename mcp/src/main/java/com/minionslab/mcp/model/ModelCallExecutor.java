package com.minionslab.mcp.model;

import java.util.concurrent.CompletableFuture;

public interface ModelCallExecutor {
    CompletableFuture<MCPModelCallResponse> execute();
} 