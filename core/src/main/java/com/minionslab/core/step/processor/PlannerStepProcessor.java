package com.minionslab.core.step.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.definition.StepDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PlannerStepProcessor implements Processor<StepContext> {
    
    private static final Logger log = LoggerFactory.getLogger(PlannerStepProcessor.class);
    private final ListableBeanFactory beanFactory;
    private final ChainRegistry chainRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StepDefinitionService stepDefinitionService;
    
    @Autowired
    public PlannerStepProcessor(ListableBeanFactory beanFactory, ChainRegistry chainRegistry, StepDefinitionService stepDefinitionService) {
        this.beanFactory = beanFactory;
        this.chainRegistry = chainRegistry;
        this.stepDefinitionService = stepDefinitionService;
    }
    
    @Override
    public boolean accepts(StepContext input) {
        return "planner".equals(input.getStep().getType());
    }
    
    @Override
    public StepContext process(StepContext input) {
        List<String> schemas = null;
        try {
            schemas = stepDefinitionService.generateStepDefinitionStrings();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Message> messages = new ArrayList<>();
        messages.add(input.getStep().getSystemPrompt());
        
        for (String value : schemas) {
            messages.add(new DefaultMessage(MessageScope.STEP, MessageRole.SYSTEM, value, Map.of()));
        }
        input.addModelCall(new ModelCall(ModelConfig.builder().build(), new MessageBundle(messages)));
        return input;
    }
    
    
}
