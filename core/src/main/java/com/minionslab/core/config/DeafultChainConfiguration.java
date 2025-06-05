package com.minionslab.core.config;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.agent.AgentProcessor;
import com.minionslab.core.common.chain.*;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.StepService;
import com.minionslab.core.step.processor.StepProcessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DeafultChainConfiguration {
    
    
    private final ObjectProvider<List<ChainCustomizer>> chainCustomizers;
    private final ObjectProvider<List<ProcessorCustomizer>> processorCustomizers;
    private final StepService stepService;
    
    public DeafultChainConfiguration(ObjectProvider<List<ChainCustomizer>> chainCustomizers,
                                     ObjectProvider<List<ProcessorCustomizer>> processorCustomizers, StepService stepService) {
        this.chainCustomizers = chainCustomizers;
        
        this.processorCustomizers = processorCustomizers;
        this.stepService = stepService;
    }
    
    @Bean(name = "agentProcessorChain")
    public Chain<AgentProcessor, AgentContext> getAgentProcessorChain() {
        Chain chain = new AbstractBaseChain(null, processorCustomizers) {
            @Override
            protected void registerProcessors() {
                this.addToStart(new AgentProcessor(stepService));
            }
            
            @Override
            public boolean accepts(ProcessContext context) {
                return context instanceof AgentContext;
            }
        };
        return chain;
        
    }
    
//    @Bean(name = "stepProcessorChain")
    public Chain<StepProcessor, StepContext> getStepProcessorChain() {
        Chain chain = new AbstractBaseChain(null, processorCustomizers) {
            @Override
            protected void registerProcessors() {
                this.addToStart(new AgentProcessor(stepService));
            }
            
            @Override
            public boolean accepts(ProcessContext context) {
                return context instanceof AgentContext;
            }
        };
        return chain;
        
    }
} 