package com.minionslab.core.step.completion;

import com.minionslab.core.common.chain.AbstractProcessor;
import org.springframework.stereotype.Component;

@Component
public class ModelSignaledCompletionProcessor extends AbstractProcessor<StepCompletionContext, StepCompletionOutcome> {
    
    
    @Override
    protected StepCompletionOutcome doProcess(StepCompletionContext input) throws Exception {
        String modelSignaledCompletionOutput = input.getModelSignaledCompletionOutput();
        if (modelSignaledCompletionOutput != null && modelSignaledCompletionOutput.equals("STEP_COMPLETE")) {
            return StepCompletionOutcome.COMPLETE;
        }
        return null;
    }
}

