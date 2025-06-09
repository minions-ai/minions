package com.minionslab.core.agent.processor;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.agent.SummarizationConfig;
import com.minionslab.core.common.chain.AbstractProcessor;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.common.message.MessageRole;
import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.expression.Expr;
import com.minionslab.core.memory.query.expression.ExprUtil;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.service.ModelCallService;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class MemorySummarizerProcessor extends AbstractProcessor<AgentContext, List<Message>> {
    private final ModelCallService modelCallService;
    
    public MemorySummarizerProcessor(ModelCallService modelCallService) {
        super();
        this.modelCallService = modelCallService;
    }
    
    @Override
    public boolean accepts(AgentContext input) {
        return true;
    }
    
    
    @Override
    protected List<Message> doProcess(AgentContext input) throws Exception {
        // Get the memory manager
        var memoryManager = input.getMemoryManager();
        if (memoryManager == null) {
            // If not available, return input as is
            throw new MemoryNotAvailableException("MemoryManager is not available.");
        }
        // Get last N user and assistant messages (e.g., 10 each)
        SummarizationConfig summarizationConfig = AgentContext.getConfig().getSummarizationConfig();
        int limit = summarizationConfig.getInputMessageLimit();
        MemoryQuery query = MemoryQuery.builder()
                                       .expression(ExprUtil.getConversationIdExpression(input.getConversationId()).and(Expr.or(Expr.eq("role", MessageRole.ASSISTANT), Expr.eq(
                                               "role", MessageRole.USER), Expr.eq("role", MessageRole.GOAL)))).build();
        
        List<Message> memoryMessages = memoryManager.query(query);
        
        
        memoryMessages.sort(Comparator.comparing(Message::getTimestamp));
        // Build a MessageBundle
        MessageBundle bundle = new MessageBundle(memoryMessages);
        // Get ModelConfig from metadata if available, else use a default
        
        ModelConfig modelConfig = summarizationConfig.getModelConfig();
        if (modelConfig != null) {
            // Provide a default/placeholder config (should be replaced with real config in production)
            modelConfig = getDefaultModelConfig();
        }
        // Create the ModelCall
        ModelCall modelCall = new ModelCall(modelConfig, bundle);
        modelCallService.call(modelCall);
        List<Message> messages = modelCall.getResponse().getMessages();
//todo we need a way to update all messages with recipeId, conversationId ...
        memoryManager.storeAll(messages, MemorySubsystem.EPISODIC);
        memoryManager.storeAll(messages, MemorySubsystem.VECTOR);
        return messages;
    }
    
    //todo read this from a global config
    private static ModelConfig getDefaultModelConfig() {
        return ModelConfig.builder()
                          .modelId("gpt-4")
                          .provider("openai")
                          .version("2024-01-01")
                          .maxContextLength(4096)
                          .maxTokens(512)
                          .temperature(0.7)
                          .topP(1.0)
                          .build();
    }
}
