package com.minionslab.core.service;

import com.minionslab.core.common.chain.AbstractBaseChain;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.ProcessorCustomizer;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.service.adaptor.SpringAIModelAdaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelCallChain extends AbstractBaseChain<AIModelProvider, ModelCall> {
    
    
    private final SpringAIModelAdaptor springAIModelAdaptor;
    
    public ModelCallChain(ObjectProvider<List<AIModelProvider>> providers, ObjectProvider<List<ProcessorCustomizer>> processorCustomizers, SpringAIModelAdaptor springAIModelAdaptor) {
        super(providers, processorCustomizers);
        this.springAIModelAdaptor = springAIModelAdaptor;
    }
    
    @Override
    protected void registerProcessors() {
        this.addToEnd(springAIModelAdaptor);
    }
    
    /**
     * Returns true if any processor in the chain accepts the given context.
     *
     * @param context the context to check
     * @return true if any processor accepts, false otherwise
     */
    @Override
    public boolean accepts(ProcessContext context) {
        if(!(context instanceof ModelCall)){
            return false;
        }
        boolean accepted = false;
        for(AIModelProvider processor: processors){
            accepted = accepted || processor.accepts((ModelCall) context);
        }
        return accepted;
    }
}
