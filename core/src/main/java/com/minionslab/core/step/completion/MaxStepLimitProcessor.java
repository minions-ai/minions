package com.minionslab.core.step.completion;

import com.minionslab.core.common.chain.AbstractProcessor;
import org.springframework.stereotype.Component;

@Component
public class MaxStepLimitProcessor extends AbstractProcessor<StepCompletionContext, StepCompletionOutcome> {
    
    
    @Override
    protected StepCompletionOutcome doProcess(StepCompletionContext input) throws Exception {
//todo figure out how to limit the number of steps
        return null;
    }
}
