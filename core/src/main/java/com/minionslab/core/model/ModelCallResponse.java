package com.minionslab.core.model;

import com.minionslab.core.message.Message;
import com.minionslab.core.tool.ToolCall;
import com.minionslab.core.util.MessageConverter;
import org.springframework.ai.chat.model.ChatResponse;
import com.minionslab.core.step.StepCompletionOutputInstructions.StepCompletionInstruction;

import java.util.List;

public class ModelCallResponse {
    private final List<Message> messages;
    private final List<ToolCall> toolCalls;
    private StepCompletionInstruction completionInstruction;
    
    public ModelCallResponse(ChatResponse chatResponse) {
        this.messages = MessageConverter.toMCPMessages(
                chatResponse.getResults().stream().map(g -> (org.springframework.ai.chat.messages.Message) g.getOutput()).toList()
                                                      );
        this.toolCalls = chatResponse.getResults().stream()
                                     .flatMap(g -> g.getOutput().getToolCalls().stream())
                                     .map(MessageConverter::fromSpringToolCall)
                                     .toList();
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }

    public StepCompletionInstruction getCompletionInstruction() {
        return completionInstruction;
    }

    public void setCompletionInstruction(StepCompletionInstruction completionInstruction) {
        this.completionInstruction = completionInstruction;
    }
} 