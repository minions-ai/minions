package com.minionslab.mcp.memory;

import com.minionslab.mcp.agent.AgentRecipe;
import com.minionslab.mcp.memory.springai.SpringAIChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MCPChatMemoryFactory {
    @Autowired
    private ChatMemoryRepository chatMemoryRepository;
    
    public MCPChatMemory create(AgentRecipe recipe) {
        switch (recipe.getMemoryType()) {
            case "springai":
                // TODO: Implement SpringAIChatMemory instantiation
                throw new UnsupportedOperationException("SpringAIChatMemory not implemented");
            case "inMemory":
            default:
                return new SpringAIChatMemory(chatMemoryRepository);
        }
    }
} 