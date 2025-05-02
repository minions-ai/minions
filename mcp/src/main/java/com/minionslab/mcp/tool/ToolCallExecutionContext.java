package com.minionslab.mcp.tool;

import lombok.Builder;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;

@Builder
public record ToolCallExecutionContext(
    ToolCallingManager toolCallingManager,
    ChatMemoryRepository chatMemory,
    String conversationId,
    ToolCallingChatOptions chatOptions,
    ChatModel chatModel
) {} 