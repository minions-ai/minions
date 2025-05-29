package com.minionslab.core.service;

import com.minionslab.core.common.chain.AbstractBaseChain;
import com.minionslab.core.common.chain.ProcessorCustomizer;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.service.adaptor.SpringAIModelAdaptor;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

public class ModelCallChain extends AbstractBaseChain<AIModelProvider, ModelCall> {
    
    
    public ModelCallChain(ObjectProvider<List<AIModelProvider>> providers, ObjectProvider<List<ProcessorCustomizer>> processorCustomizers) {
        super(providers, processorCustomizers);
        
    }
    
    @Override
    protected void registerProcessors() {
        this.addToEnd(new SpringAIModelAdaptor());
    }
}
