package com.minionslab.mcp.model;

import com.minionslab.mcp.message.MCPMessage;
import com.minionslab.mcp.step.Step;
import com.minionslab.mcp.tool.MCPToolCall;
import com.minionslab.mcp.util.MCPtoSpringConverter;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;

import java.util.List;

public class MCPModelCallResponse {
    private final List<MCPMessage> messages;
    private final Step.StepInstruction instruction;
    private final List<MCPToolCall> toolCalls;

    public MCPModelCallResponse(ChatResponse chatResponse) {
        this.messages = MCPtoSpringConverter.toMCPMessages(
            chatResponse.getResults().stream().map(g -> (Message) g.getOutput()).toList()
                                                          );
        this.instruction = extractStepInstruction(chatResponse);
        this.toolCalls = chatResponse.getResults().stream()
                .flatMap(g -> g.getOutput().getToolCalls().stream())
                .map(MCPtoSpringConverter::fromSpringToolCall)
                .toList();
    }

    private Step.StepInstruction extractStepInstruction(ChatResponse chatResponse) {
        try {
            // Use the first result's output text for instruction extraction
            String text = chatResponse.getResult().getOutput().getText();
            BeanOutputConverter<Step.StepInstruction> converter = new BeanOutputConverter<>(Step.StepInstruction.class);
            return converter.convert(text);
        } catch (Exception e) {
            // Could not extract instruction; return null
            return null;
        }
    }

    public List<MCPMessage> getMessages() {
        return messages;
    }
    public Step.StepInstruction getInstruction() {
        return instruction;
    }
    public List<MCPToolCall> getToolCalls() {
        return toolCalls;
    }
} 