package com.minionslab.mcp.tool.springai;

import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.memory.MCPChatMemory;
import com.minionslab.mcp.tool.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Executes a tool call using the Spring AI Tool infrastructure, with robust error handling.
 * This class is responsible for invoking the correct tool callback, handling all edge cases,
 * and updating the tool call status and response accordingly.
 */
@Slf4j
public class SpringAIToolCallExecutor implements ToolCallExecutor {
    @Getter
    private final MCPToolCall toolCall;
    private final MCPContext context;
    private final ToolRegistry toolRegistry;
    private final Executor executor;
    private final MCPChatMemory mcpChatMemory;

    public SpringAIToolCallExecutor(MCPToolCall toolCall, MCPContext context, ToolRegistry toolRegistry) {
        this(toolCall, context, toolRegistry, ForkJoinPool.commonPool());
    }

    public SpringAIToolCallExecutor(MCPToolCall toolCall,
                                    MCPContext context, ToolRegistry toolRegistry,
                                    Executor executor) {
        this.toolCall = toolCall;
        this.context = context;
        this.toolRegistry = toolRegistry;
        this.executor = executor;
        this.mcpChatMemory = context.getChatMemory();
    }

    /**
     * Executes the tool call asynchronously, handling all error and edge cases.
     * @return A CompletableFuture with the tool call response.
     */
    @Override
    public CompletableFuture<MCPToolCall.MCPToolCallResponse> execute() {
        return CompletableFuture.supplyAsync(() -> {
            // Validate tool name
            if (toolCall.getName() == null || toolCall.getName().isEmpty()) {
                return failToolCall("Tool name is null or empty");
            }
            // Validate request
            if (toolCall.getRequest() == null) {
                return failToolCall("Tool call request is null");
            }
            // Lookup tool callback
            ToolCallback toolCallback = toolRegistry.getTool(toolCall.getName());
            if (toolCallback == null) {
                return failToolCall("Tool not found: " + toolCall.getName());
            }
            // Execute the tool call
            return doToolCall(toolCallback);
        }, executor);
    }

    /**
     * Helper to handle tool callback execution and error handling.
     */
    private MCPToolCall.MCPToolCallResponse doToolCall(ToolCallback toolCallback) {
        toolCall.setStatus(ToolCallStatus.EXECUTING);
        try {
            ToolContext toolContext = new ToolContext(toolCall.getRequest().parameters());
            String callResult = toolCallback.call(toolCall.getRequest().input(), toolContext);
            MCPToolCall.MCPToolCallResponse response = new MCPToolCall.MCPToolCallResponse(callResult, null);
            toolCall.setResponse(response);
            toolCall.setStatus(ToolCallStatus.COMPLETED);
            log.info("Tool call execution completed successfully: {}", toolCall.getName());
            return response;
        } catch (Exception toolEx) {
            String error = "Tool callback execution failed: " + toolEx.getMessage();
            log.error(error, toolEx);
            return failToolCall(error);
        }
    }

    /**
     * Helper to set tool call as failed and return a failed response.
     */
    private MCPToolCall.MCPToolCallResponse failToolCall(String error) {
        log.error(error);
        toolCall.setStatus(ToolCallStatus.FAILED);
        MCPToolCall.MCPToolCallResponse response = new MCPToolCall.MCPToolCallResponse(null, error);
        toolCall.setResponse(response);
        return response;
    }
} 