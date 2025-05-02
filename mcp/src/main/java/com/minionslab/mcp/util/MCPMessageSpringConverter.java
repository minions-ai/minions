package com.minionslab.mcp.util;

import com.minionslab.mcp.message.DefaultMCPMessage;
import com.minionslab.mcp.message.MCPMessage;
import com.minionslab.mcp.message.MessageRole;
import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.model.MCPModelCall.MCPModelCallRequest;
import com.minionslab.mcp.tool.MCPToolCall;
import com.minionslab.mcp.tool.MCPToolCall.MCPToolCallRequest;
import com.minionslab.mcp.tool.MCPToolCall.MCPToolCallResponse;
import groovyjarjarantlr4.v4.tool.ToolMessage;
import org.springframework.ai.chat.messages.*;
import org.springframework.messaging.support.ErrorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts between MCP message formats and Spring AI message formats.
 */
public class MCPMessageSpringConverter {
    
    /**
     * Converts a list of Spring AI messages to MCP messages.
     *
     * @param springMessages The Spring AI messages to convert
     * @return The converted MCP messages
     */
    public static List<MCPMessage> toMCPMessages(List<Message> springMessages) {
        return springMessages.stream()
                             .<MCPMessage>map(MCPMessageSpringConverter::toMCPMessage)
                             .collect(Collectors.toList());
    }
    
    /**
     * Converts a single Spring AI message to an MCP message.
     *
     * @param springMessage The Spring AI message to convert
     * @return The converted MCP message
     */
    public static MCPMessage toMCPMessage(Message springMessage) {
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
        return DefaultMCPMessage.builder()
                                .role(role)
                                .content(springMessage.getText())
                                .build();
    }
    
    private static List<ToolResponseMessage.ToolResponse> fromMCPToolCall(List<MCPToolCall> toolCalls) {
        List<ToolResponseMessage.ToolResponse> result = new ArrayList<>();
        if (toolCalls != null) {
            for (MCPToolCall mcpToolCall : toolCalls) {
                String name = mcpToolCall.getRequest() != null ? mcpToolCall.getRequest().name() : null;
                String response = mcpToolCall.getResponse() != null ? mcpToolCall.getResponse().response() : null;
                ToolResponseMessage.ToolResponse toolResponse = new ToolResponseMessage.ToolResponse(
                        UUID.randomUUID().toString(), name, response);
                result.add(toolResponse);
            }
        }
        return result;
    }
    
    // Convert Spring ToolResponseMessage.ToolResponse to MCPToolCall
    public static List<MCPToolCall> fromSpringToolResponses(List<ToolResponseMessage.ToolResponse> toolResponses) {
        List<MCPToolCall> mcpResponses = new ArrayList<>();
        for (ToolResponseMessage.ToolResponse toolResponse : toolResponses) {
            MCPToolCallRequest request = new MCPToolCallRequest(toolResponse.name(), null, null);
            MCPToolCallResponse response = new MCPToolCallResponse(toolResponse.responseData(), null);
            MCPToolCall mcpToolCall = MCPToolCall.builder()
                                                 .request(request)
                                                 .response(response)
                                                 .build();
            mcpResponses.add(mcpToolCall);
        }
        return mcpResponses;
    }
    
    // Not used, but implemented for completeness
    private static MCPToolCallResponse fromSpringToolResponse(String s) {
        return new MCPToolCallResponse(s, null);
    }
    

    
    /**
     * Converts a list of MCP messages to Spring AI messages.
     *
     * @param mcpMessages The MCP messages to convert
     * @return The converted Spring AI messages
     */
    public static List<Message> toSpringMessages(List<MCPMessage> mcpMessages) {
        return mcpMessages.stream()
                          .map(mcpMessage -> toSpringMessage(mcpMessage))
                          .collect(Collectors.toList());
    }
    
    /**
     * Converts a single MCP message to a Spring AI message.
     *
     * @param mcpMessage The MCP message to convert
     * @return The converted Spring AI message
     */
    public static Message toSpringMessage(MCPMessage mcpMessage) {
        return switch (mcpMessage.getRole()) {
            case SYSTEM, ERROR, TOOL -> new SystemMessage(mcpMessage.getContent());
            case USER -> new UserMessage(mcpMessage.getContent());
            case ASSISTANT -> new AssistantMessage(mcpMessage.getContent());
            default -> throw new IllegalArgumentException("Unsupported message role: " + mcpMessage.getRole());
        };
    }
    
    public static MCPMessage createErrorMessage(Exception e) {
        return DefaultMCPMessage.builder()
            .role(MessageRole.ERROR)
            .content("Error: " + e.getMessage() + "\n" + 
                    "Type: " + e.getClass().getSimpleName() + "\n" +
                    (e.getCause() != null ? "Cause: " + e.getCause().getMessage() : ""))
            .build();
    }
}