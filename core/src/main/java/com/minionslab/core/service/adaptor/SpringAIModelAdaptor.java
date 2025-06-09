package com.minionslab.core.service.adaptor;

import com.minionslab.core.common.message.Message;
import com.minionslab.core.common.util.MessageConverter;
import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.model.ChatModelRepository;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.model.ModelCallResponse;
import com.minionslab.core.service.AIModelProvider;
import com.minionslab.core.tool.ToolCall;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringAIModelAdaptor implements AIModelProvider {
    
    private final ChatModelRepository chatModelRepository;
    
    @Autowired
    public SpringAIModelAdaptor(ChatModelRepository chatModelRepository) {
        this.chatModelRepository = chatModelRepository;
    }
    
    @Override
    public boolean accepts(ModelCall input) {
        ModelConfig modelConfig = input.getModelConfig();
        if (modelConfig == null) return false;
        try {
            chatModelRepository.getChatModel(modelConfig.getProvider(), modelConfig.getModelId());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public ModelCall process(ModelCall input) {
        ModelConfig modelConfig = input.getModelConfig();
        if (modelConfig == null) throw new IllegalArgumentException("ModelConfig is required");
        
        ChatModel chatModel = chatModelRepository.getChatModel(modelConfig.getProvider(), modelConfig.getModelId());
        
        // Convert your domain messages to Spring AI messages
        List<org.springframework.ai.chat.messages.Message> springMessages =
                MessageConverter.toSpringMessages(input.getRequest().messages());
        
        // Build the Prompt (add tool call options if needed)
        Prompt prompt = new Prompt(springMessages);
        
        // Call the model
        ChatResponse chatResponse = chatModel.call(prompt);
        
        // Convert the response
        ModelCallResponse response = extractResponse(chatResponse);
        
        // Attach the response to the ModelCall and return (or return a new ModelCall if you prefer immutability)
        input.setResponse(response);
        return input;
    }
    
    private ModelCallResponse extractResponse(ChatResponse chatResponse) {
        List<Message> messages = MessageConverter.toMCPMessages(
                chatResponse.getResults().stream()
                            .map(g -> (org.springframework.ai.chat.messages.Message) g.getOutput())
                            .toList()
                                                               );
        List<ToolCall> toolCalls = chatResponse.getResults().stream()
                                               .flatMap(g -> g.getOutput().getToolCalls().stream())
                                               .map(MessageConverter::fromSpringToolCall)
                                               .toList();
        
        return new ModelCallResponse(messages, toolCalls);
    }
}