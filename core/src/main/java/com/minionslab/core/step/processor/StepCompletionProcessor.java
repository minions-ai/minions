package com.minionslab.core.step.processor;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.step.StepContext;
import org.springframework.stereotype.Component;

@Component
public class StepCompletionProcessor implements Processor<StepContext> {
    
    
    private final ChainRegistry chainRegistry;
    
    public StepCompletionProcessor(ChainRegistry chainRegistry) {
        this.chainRegistry = chainRegistry;
    }
    
    
    @Override
    public boolean accepts(StepContext input) {
        return true;
    }
    
    @Override
    public StepContext process(StepContext input) {
        StepContext process = (StepContext) chainRegistry.process(input);
        return input;
    }
}
