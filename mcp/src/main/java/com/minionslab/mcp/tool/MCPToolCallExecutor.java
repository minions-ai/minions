package com.minionslab.mcp.tool;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Handles the execution of tool calls, including prompt preparation,
 * tool execution, and response handling.
 */
@Slf4j
public class MCPToolCallExecutor {
    @Getter
    private final MCPToolCall toolCall;
    private final ToolCallExecutionContext context;
    private final Executor executor;
    
    public MCPToolCallExecutor(MCPToolCall toolCall, ToolCallExecutionContext context) {
        this(toolCall, context, ForkJoinPool.commonPool());
    }
    
    public MCPToolCallExecutor(MCPToolCall toolCall,
                               ToolCallExecutionContext context,
                               Executor executor) {
        this.toolCall = toolCall;
        this.context = context;
        this.executor = executor;
    }
    
    /**
     * Creates a new executor for a tool call.
     *
     * @param toolCall The tool call to execute
     * @param context  The execution context
     * @return A new tool call executor
     */
    public static MCPToolCallExecutor forCall(MCPToolCall toolCall,
                                              ToolCallExecutionContext context) {
        return new MCPToolCallExecutor(toolCall, context);
    }
    
    /**
     * Executes the tool call asynchronously.
     *
     * @return A future containing the tool call response
     */
    public CompletableFuture<MCPToolCall.MCPToolCallResponse> execute() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                toolCall.setStatus(ToolCallStatus.EXECUTING);
                
                // Prepare prompt for tool call
                var prompt = new Prompt(
                        context.chatMemory().findByConversationId(context.conversationId()),
                        context.chatOptions()
                );
                
                // Execute the tool call
                var toolResult = context.toolCallingManager().executeToolCalls(prompt, null);
                
                // Update chat memory with tool result
                var latest = toolResult.conversationHistory() != null && !toolResult.conversationHistory().isEmpty()
                                     ? toolResult.conversationHistory().get(toolResult.conversationHistory().size() - 1)
                                     : null;
                if (latest != null) {
                    context.chatMemory().saveAll(context.conversationId(), List.of(latest));
                }
                
                // Create response
                var response = new MCPToolCall.MCPToolCallResponse(
                        latest.getText(),
                        null
                );
                
                toolCall.setResponse(response);
                toolCall.setStatus(ToolCallStatus.COMPLETED);
                
                return response;
                
            } catch (Exception e) {
                log.error("Tool call execution failed: {}", e.getMessage(), e);
                toolCall.setStatus(ToolCallStatus.FAILED);
                var response = new MCPToolCall.MCPToolCallResponse(null, e.getMessage());
                toolCall.setResponse(response);
                throw new ToolCallExecutionException("Tool call failed", e);
            }
        }, executor);
    }
} 