package com.hls.minions.model;

// Functional interface for tool execution
@FunctionalInterface
public interface ToolExecutor {
    void execute(Object... args);
}
