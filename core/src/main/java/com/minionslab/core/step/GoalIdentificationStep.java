package com.minionslab.core.step;

import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.tool.ToolCall;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GoalIdentificationStep implements Step {
    private final String id = "goal_identification";
    private final MessageBundle messageBundle;
    private final String userMessage;
    private StepExecution stepExecution;
    
    public GoalIdentificationStep(String userMessage) {
        this.userMessage = userMessage;
        this.messageBundle = new MessageBundle();
        this.messageBundle.addMessage(DefaultMessage.builder()
                                                    .role(MessageRole.GOAL)
                                                    .scope(MessageScope.STEP)
                                                    .content("Identify the agent's goal from the user message.")
                                                    .build());
        this.messageBundle.addMessage(DefaultMessage.builder()
                                                    .role(MessageRole.SYSTEM)
                                                    .scope(MessageScope.STEP)
                                                    .content("You are an agent responsible to handle what your goal is")
                                                    .build());
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public Set<String> getAvailableTools() {
        return Collections.emptySet();
    }
    
    @Override
    public Message getGoal() {
        return messageBundle.getMessages(MessageRole.GOAL).stream().findFirst().orElse(null);
    }
    
    @Override
    public ModelCall createInitialModelCall() {
        // Add the user message as a USER role message for the model call
        MessageBundle callBundle = new MessageBundle();
        callBundle.addMessages(MessageRole.SYSTEM, messageBundle.getMessages(MessageRole.SYSTEM));
        callBundle.addMessages(MessageRole.GOAL, messageBundle.getMessages(MessageRole.GOAL));
        callBundle.addMessage(DefaultMessage.builder()
                                            .role(MessageRole.USER)
                                            .scope(MessageScope.STEP)
                                            .content(userMessage)
                                            .build());
        return new ModelCall(new ModelCall.ModelCallRequest(
                callBundle.getAllMessages(),
                Map.of()
        ));
    }
    
    @Override
    public ModelCall createFollowUpModelCall(ModelCall previousModelCall, java.util.List<ToolCall> toolCalls) {
        // No follow-up model call needed for this step
        return null;
    }
    
    @Override
    public StepCompletionInstruction getCompletionCriteria() {
        return execution -> execution.getModelCalls().size() >= 1;
    }
    
    @Override
    public StepExecution getStepExecution() {
        return stepExecution;
    }
    
    @Override
    public void setStepExecution(StepExecution execution) {
        this.stepExecution = execution;
    }
    
    @Override
    public Optional<ToolCall> getDecisionToolCall() {
        return Optional.empty();
    }
    
    @Override
    public Message getSystemPrompt() {
        return messageBundle.getMessages(MessageRole.SYSTEM).stream().findFirst().orElse(null);
    }
    
    public MessageBundle getMessageBundle() {
        return messageBundle;
    }
} 