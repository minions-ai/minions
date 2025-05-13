package com.minionslab.core.model;

import java.util.concurrent.CompletableFuture;

public interface ModelCallExecutor {
    CompletableFuture<ModelCallResponse> executeAsync();
    
    ModelCallResponse execute();
}