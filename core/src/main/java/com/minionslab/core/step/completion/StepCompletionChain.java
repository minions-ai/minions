package com.minionslab.core.step.completion;

import com.minionslab.core.common.chain.AbstractBaseChain;
import com.minionslab.core.common.chain.ProcessorCustomizer;
import com.minionslab.core.step.processor.StepCompletionProcessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StepCompletionChain extends AbstractBaseChain<StepCompletionProcessor,StepCompletionContext> {
    


    public StepCompletionChain(ObjectProvider<List<StepCompletionProcessor>> processors, ObjectProvider<List<ProcessorCustomizer>> customizers) {
        super(processors,customizers);

    }
    @Override
    protected void registerProcessors() {
    
    
    }
}
