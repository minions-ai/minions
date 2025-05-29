package com.minionslab.core.step.completion;

import com.minionslab.core.common.chain.AbstractProcessor;
import org.springframework.stereotype.Component;

@Component
public class MemoryUpdateFailureProcessor extends AbstractProcessor<StepCompletionContext, StepCompletionOutcome> {
    @Override
    protected StepCompletionOutcome doProcess(StepCompletionContext input) throws Exception {
        if (input.isMemoryUpdateFailed()) {
            return StepCompletionOutcome.COMPLETE;
        }
        return null;
    }
}
