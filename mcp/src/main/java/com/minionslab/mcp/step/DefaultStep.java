package com.minionslab.mcp.step;

import com.minionslab.mcp.message.DefaultMCPMessage;
import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.tool.MCPToolCall;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Robust, extensible default implementation of Step.
 */
public class DefaultStep implements Step {
    private final String id;
    private final String description;
    private final Set<String> availableTools;
    private final Function<Map<String, Object>, String> promptSupplier;
    private final Map<String, ToolCallConfig> toolCallConfigs;
    private final StepCompletionCriteria completionCriteria;
    private final Map<String, Object> metadata;
    private final BiFunction<MCPModelCall, List<MCPToolCall>, MCPModelCall> followUpModelCallStrategy;
    private StepExecution stepExecution;
    
    public DefaultStep(String id, String description, Set<String> availableTools, String prompt) {
        this(id, description, availableTools, ctx -> prompt, null, null, null, null);
    }
    
    public DefaultStep(
            String id,
            String description,
            Set<String> availableTools,
            Function<Map<String, Object>, String> promptSupplier,
            Map<String, ToolCallConfig> toolCallConfigs,
            StepCompletionCriteria completionCriteria,
            Map<String, Object> metadata,
            BiFunction<MCPModelCall, List<MCPToolCall>, MCPModelCall> followUpModelCallStrategy
                      ) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.description = Objects.requireNonNullElse(description, "");
        this.availableTools = availableTools != null ? Set.copyOf(availableTools) : Set.of();
        this.promptSupplier = promptSupplier != null ? promptSupplier : ctx -> "";
        this.toolCallConfigs = toolCallConfigs != null ? Map.copyOf(toolCallConfigs) : Map.of();
        this.completionCriteria = completionCriteria != null ? completionCriteria : defaultCompletionCriteria();
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.followUpModelCallStrategy = followUpModelCallStrategy != null ? followUpModelCallStrategy : this::defaultFollowUpModelCall;
    }
    
    private MCPModelCall defaultFollowUpModelCall(MCPModelCall prev, List<MCPToolCall> toolCalls) {
        // By default, just create a new model call with the same prompt and new tool calls
        return getMcpModelCall(metadata);
    }
    
    private @NotNull MCPModelCall getMcpModelCall(Map<String, Object> context) {
        String prompt = promptSupplier.apply(context != null ? context : Map.of());
        return new MCPModelCall(
                new MCPModelCall.MCPModelCallRequest(
                        List.of(
                                DefaultMCPMessage.builder()
                                                 .role(com.minionslab.mcp.message.MessageRole.USER)
                                                 .content(prompt)
                                                 .build()
                               ),
                        Map.of()
                )
        );
    }
    
    private static StepCompletionCriteria defaultCompletionCriteria() {
        return new StepCompletionCriteria() {
            @Override
            public boolean isComplete(StepExecution execution) {
                // Default: complete after one model call
                return execution.getModelCalls().size() >= 1;
            }
            
        };
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public Set<String> getAvailableTools() {
        return availableTools;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public MCPModelCall createInitialModelCall() {
        return getMcpModelCall(metadata);
    }
    
    
    @Override
    public MCPModelCall createFollowUpModelCall(MCPModelCall prev, List<MCPToolCall> toolCalls) {
        return followUpModelCallStrategy.apply(prev, toolCalls);
    }
    
    @Override
    public StepCompletionCriteria getCompletionCriteria() {
        return completionCriteria;
    }
    
    @Override
    public StepExecution getStepExecution() {
        return stepExecution;
    }
    
    @Override
    public void setStepExecution(StepExecution execution) {
        this.stepExecution = execution;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public record ToolCallConfig(String parameters, String explanation) {
        public ToolCallConfig(String parameters, String explanation) {
            this.parameters = parameters != null ? parameters : "";
            this.explanation = explanation != null ? explanation : "";
        }
    }
}
