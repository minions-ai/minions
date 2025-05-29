package com.minionslab.core.step.processor;

import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.memory.MemoryQueryUtils;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.config.ModelConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SummarizeStepProcessor implements Processor<StepContext> {
    @Override
    public boolean accepts(StepContext input) {
        return true;
    }
    
    @Override
    public StepContext process(StepContext input) {
        // Get the memory manager
        var memoryManager = input.getMemoryManager();
        if (memoryManager == null) {
            // If not available, return input as is
            return input;
        }
        // Get last N user and assistant messages (e.g., 10 each)
        int n = 10;
        List<Message> userMessages = MemoryQueryUtils.getLastNUserMessages(memoryManager, n);
        List<Message> assistantMessages = MemoryQueryUtils.getLastNAssistantMessages(memoryManager, n);
        // Merge and sort by timestamp (if needed)
        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(userMessages);
        allMessages.addAll(assistantMessages);
        allMessages.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        // Build a MessageBundle
        MessageBundle bundle = new MessageBundle(allMessages);
        // Get ModelConfig from metadata if available, else use a default
        ModelConfig modelConfig = null;
        Object configObj = input.getMetadata().get("modelConfig");
        if (configObj instanceof ModelConfig) {
            modelConfig = (ModelConfig) configObj;
        } else {
            // Provide a default/placeholder config (should be replaced with real config in production)
            modelConfig = ModelConfig.builder()
                .modelId("gpt-4")
                .provider("openai")
                .version("2024-01-01")
                .maxContextLength(4096)
                .maxTokens(512)
                .temperature(0.7)
                .topP(1.0)
                .build();
        }
        // Create the ModelCall
        ModelCall modelCall = new ModelCall(modelConfig, bundle);
        // Add the ModelCall to the StepContext
        input.addModelCall(modelCall);
        // Optionally, set the prompt (if needed by downstream processors)
        // input.setPrompt(new Prompt(allMessages, Map.of()));
        return input;
    }
}
