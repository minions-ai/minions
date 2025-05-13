package com.minionslab.core.memory;

import com.minionslab.core.agent.Agent;
import com.minionslab.core.agent.AgentRecipe;
import com.minionslab.core.memory.springai.SpringAIChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModelMemoryFactory {
    @Autowired
    private ChatMemoryRepository chatMemoryRepository;
    
    public ModelMemory create(Agent recipe) {
        return new AgentModelMemory();
    }
} 