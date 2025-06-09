package com.minionslab.core.step.processor;

import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.StepService;
import org.springframework.stereotype.Component;

@Component
public class StepCompletionProcessor implements StepProcessor {
    
    
    private final StepService stepService;
    
    public StepCompletionProcessor(StepService stepService) {
        this.stepService = stepService;
    }
    
    @Override
    public boolean accepts(StepContext input) {
        return true;
    }
    
    @Override
    public StepContext process(StepContext input) {
//        StepContext processed = stepService.executeStep(input);
        //todo this step processor is not implemented properly. Calling stepService from here causes an infitie call loop and a stack overflow
        return input;
    }
}
