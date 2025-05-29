package com.minionslab.core.service.adaptor;

import com.minionslab.core.common.util.MessageConverter;
import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.message.Message;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.model.ModelCallResponse;
import com.minionslab.core.service.AIModelProvider;
import com.minionslab.core.tool.ToolCall;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SpringAIModelAdaptor implements AIModelProvider {
    @Override
    public boolean accepts(ModelCall input) {
        ModelConfig modelConfig = input.getModelConfig();
        AtomicBoolean modelFound = new AtomicBoolean(false);
        if (modelConfig != null) {
            String modelId = modelConfig.getModelId();
            String provider = modelConfig.getProvider();
            if (modelId != null) {
                //todo this is not complete. Currently it only checks for matching providers but doesn't check for matching model ids because modelId's are not available in the
                // Spring AI Models class. We should match both provider and modelId.
                ReflectionUtils.doWithFields(SpringAIModels.class, field -> {
                    if (field.getName().equals(provider)) {
                        modelFound.set(true);
                    }
                    
                });
            }
        }
        return modelFound.get();
    }
    
    @Override
    public ModelCall process(ModelCall input) {
        //todo implement this method for Spring AI Model Adaptor.
        return null;
    }
    
    
    private ModelCallResponse extractResponse(ChatResponse chatResponse) {
        
        List<Message> messages = MessageConverter.toMCPMessages(chatResponse.getResults().stream().map(g -> (org.springframework.ai.chat.messages.Message) g.getOutput()).toList());
        List<ToolCall> toolCalls = chatResponse.getResults().stream().flatMap(g -> g.getOutput().getToolCalls().stream()).map(MessageConverter::fromSpringToolCall).toList();
        
        ModelCallResponse response = new ModelCallResponse(messages, toolCalls);
        return response;
    }


}

