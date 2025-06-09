package com.minionslab.core.agent.processor;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.common.chain.AbstractProcessor;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.memory.MemoryManager;
import com.minionslab.core.memory.MemorySubsystem;

import java.util.List;

public class MemoryPrimingProcessor extends AbstractProcessor<AgentContext, List<Message>> {
    
    
    @Override
    protected List<Message> doProcess(AgentContext input) throws Exception {
        MemoryManager memoryManager = input.getMemoryManager();
        List<Message> messages = memoryManager.query(input);
        memoryManager.storeAll(messages, MemorySubsystem.SHORT_TERM);
        return messages;
    }
}
