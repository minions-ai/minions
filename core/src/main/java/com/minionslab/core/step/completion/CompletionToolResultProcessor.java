package com.minionslab.core.step.completion;

import com.minionslab.core.common.chain.AbstractProcessor;
import org.springframework.stereotype.Component;

@Component
public class CompletionToolResultProcessor extends AbstractProcessor<StepCompletionContext, StepCompletionOutcome> {
    
    @Override
    protected StepCompletionOutcome doProcess(StepCompletionContext input) throws Exception {
        String checkerToolOutput = input.getCheckerToolOutput();
        if (checkerToolOutput != null && checkerToolOutput.equals("STEP_COMPLETE")) {
            return StepCompletionOutcome.COMPLETE;
        }
        return null;
    }
}
