package com.minionslab.core.common.util;

import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.tool.ToolCall;
import org.springframework.ai.chat.messages.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts between MCP message formats and Spring AI message formats.
 */
public class MessageConverter {
    
    /**
     * Converts a list of Spring AI messages to MCP messages.
     *
     * @param springMessages The Spring AI messages to convert
     * @return The converted MCP messages
     */
    public static List<com.minionslab.core.message.Message> toMCPMessages(List<org.springframework.ai.chat.messages.Message> springMessages) {
        return springMessages.stream()
                             .<Message>map(MessageConverter::toMCPMessage)
                             .collect(Collectors.toList());
    }
    
    /**
     * Converts a single Spring AI message to an MCP message.
     *
     * @param springMessage The Spring AI message to convert
     * @return The converted MCP message
     */
    public static Message toMCPMessage(org.springframework.ai.chat.messages.Message springMessage) {
        MessageRole role;
        if (springMessage instanceof SystemMessage) {
            role = MessageRole.SYSTEM;
        } else if (springMessage instanceof UserMessage) {
            role = MessageRole.USER;
        } else if (springMessage instanceof AssistantMessage) {
            role = MessageRole.ASSISTANT;
        } else {
            throw new IllegalArgumentException("Unsupported message type: " + springMessage.getClass());
        }
        return DefaultMessage.builder()
                             .role(role)
                             .content(springMessage.getText())
                             .build();
    }
    
    private static List<ToolResponseMessage.ToolResponse> fromMCPToolCall(List<ToolCall> toolCalls) {
        List<ToolResponseMessage.ToolResponse> result = new ArrayList<>();
        if (toolCalls != null) {
            for (ToolCall toolCall : toolCalls) {
                String name = toolCall.getRequest() != null ? toolCall.getRequest().name() : null;
                String response = toolCall.getResponse() != null ? toolCall.getResponse().response() : null;
                ToolResponseMessage.ToolResponse toolResponse = new ToolResponseMessage.ToolResponse(
                        UUID.randomUUID().toString(), name, response);
                result.add(toolResponse);
            }
        }
        return result;
    }
    
    
    // Not used, but implemented for completeness
    private static ToolCall.ToolCallResponse fromSpringToolResponse(String s) {
        return new ToolCall.ToolCallResponse(s, null);
    }
    
    
    /**
     * Converts a list of MCP messages to Spring AI messages.
     *
     * @param messages The MCP messages to convert
     * @return The converted Spring AI messages
     */
    public static List<org.springframework.ai.chat.messages.Message> toSpringMessages(List<Message> messages) {
        return messages.stream()
                       .map(mcpMessage -> toSpringMessage(mcpMessage))
                       .collect(Collectors.toList());
    }
    
    /**
     * Converts a single MCP message to a Spring AI message.
     *
     * @param message The MCP message to convert
     * @return The converted Spring AI message
     */
    public static org.springframework.ai.chat.messages.Message toSpringMessage(com.minionslab.core.message.Message message) {
        return switch (message.getRole()) {
            case SYSTEM, ERROR, TOOL, GOAL -> new SystemMessage(message.toPromptString());
            case USER -> new UserMessage(message.toPromptString());
            case ASSISTANT -> new AssistantMessage(message.toPromptString());
            default -> throw new IllegalArgumentException("Unsupported message role: " + message.getRole());
        };
    }
    
    public static com.minionslab.core.message.Message createErrorMessage(Exception e) {
        return DefaultMessage.builder()
                             .role(MessageRole.ERROR)
                             .content("Error: " + e.getMessage() + "\n" +
                                                 "Type: " + e.getClass().getSimpleName() + "\n" +
                                                 (e.getCause() != null ? "Cause: " + e.getCause().getMessage() : ""))
                             .build();
    }
    
    public static ToolCall fromSpringToolCall(AssistantMessage.ToolCall toolCall) {
        if (toolCall == null)
            return null;
        return ToolCall.builder()
                       .request(new ToolCall.ToolCallRequest(
                                  toolCall.name(),
                                  toolCall.arguments() != null ? toolCall.arguments().toString() : null, null
                          ))
                       .build();
    }

}