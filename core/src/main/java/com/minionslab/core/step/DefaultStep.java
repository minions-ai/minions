package com.minionslab.core.step;

import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.tool.ToolCall;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Robust, extensible default implementation of Step.
 */
public class DefaultStep implements Step {
    private final String id;
    private final MessageBundle messageBundle;
    private final Set<String> availableTools;
    private final Map<String, ToolCallConfig> toolCallConfigs;
    private final Map<String, Object> metadata;
    private final BiFunction<ModelCall, List<ToolCall>, ModelCall> followUpModelCallStrategy;
    private final ToolCall decisionToolCall;
    private StepExecution stepExecution;

    
    
    public DefaultStep(String id, MessageBundle messageBundle) {
        this(id, messageBundle, null, null, null, null, null);
    }
    
    
    public DefaultStep(String id, MessageBundle messageBundle, Set<String> availableTools) {
        this(id, messageBundle, availableTools, null, null, null, null);
    }
    
    public DefaultStep(
            String id,
            MessageBundle messageBundle,
            Set<String> availableTools,
            Map<String, ToolCallConfig> toolCallConfigs,
            Map<String, Object> metadata,
            BiFunction<ModelCall, List<ToolCall>, ModelCall> followUpModelCallStrategy,
            ToolCall decisionToolCall
                      ) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.messageBundle = messageBundle != null ? messageBundle : new MessageBundle();
        this.availableTools = availableTools != null ? Set.copyOf(availableTools) : java.util.Set.of();
        this.toolCallConfigs = toolCallConfigs != null ? Map.copyOf(toolCallConfigs) : Map.of();
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.followUpModelCallStrategy = followUpModelCallStrategy != null ? followUpModelCallStrategy : this::defaultFollowUpModelCall;
        this.decisionToolCall = decisionToolCall;
    }
    
    private ModelCall defaultFollowUpModelCall(ModelCall prev, List<ToolCall> toolCalls) {
        // By default, just create a new model call with the same prompt and new tool calls
        return getMcpModelCall(metadata);
    }
    
    private @NotNull ModelCall getMcpModelCall(Map<String, Object> context) {
        List<Message> messages = messageBundle.getAllMessages();
        return new ModelCall(
                new ModelCall.ModelCallRequest(
                        messages,
                        Map.of()
                )
        );
    }
    
    
    private Message createMessage(String content, MessageRole role) {
        return DefaultMessage.builder()
                             .scope(MessageScope.STEP)
                             .role(role)
                             .content(content)
                             .build();
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
    public Message getGoal() {
        return messageBundle.getMessages(MessageRole.GOAL).stream().findFirst().orElse(null);
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
    
    @Override
    public Optional<ToolCall> getDecisionToolCall() {
        return Optional.ofNullable(decisionToolCall);
    }
    
    @Override
    public Message getSystemPrompt() {
        return messageBundle.getMessages(MessageRole.SYSTEM).stream().findFirst().orElse(null);
    }
    
    public MessageBundle getMessageBundle() {
        return messageBundle;
    }
    
    public record ToolCallConfig(String parameters, String explanation) {
        public ToolCallConfig(String parameters, String explanation) {
            this.parameters = parameters != null ? parameters : "";
            this.explanation = explanation != null ? explanation : "";
        }
    }
}
