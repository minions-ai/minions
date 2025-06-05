package com.minionslab.core.step.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.common.chain.AbstractProcessor;
import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.message.SimpleMessage;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.definition.StepDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlannerStepProcessor extends AbstractProcessor<StepContext, List<ModelCall>> implements StepProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(PlannerStepProcessor.class);
    
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StepDefinitionService stepDefinitionService;
    
    @Autowired
    public PlannerStepProcessor(StepDefinitionService stepDefinitionService) {
        this.stepDefinitionService = stepDefinitionService;
    }
    
    @Override
    public boolean accepts(StepContext input) {
        return "planner".equals(input.getStep().getType());
    }
    
    
    @Override
    protected List<ModelCall> doProcess(StepContext input) throws Exception {
        List<String> schemas = null;
        try {
            schemas = stepDefinitionService.generateStepDefinitionStrings();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Message> messages = new ArrayList<>();
        messages.add(input.getStep().getSystemPrompt());
        
        for (String value : schemas) {
            messages.add(SimpleMessage.builder().scope(MessageScope.STEP).role(MessageRole.SYSTEM).content(value).build());
            
        }
        ModelCall modelCall = new ModelCall(ModelConfig.builder().build(), new MessageBundle(messages));
        input.addModelCall(modelCall);
        return List.of(modelCall);
    }
    
    
}
