package com.minionslab.core.step.completion;

import com.minionslab.core.common.chain.AbstractProcessor;
import org.springframework.stereotype.Component;

@Component
public class ExternalAbortProcessor extends AbstractProcessor<StepCompletionContext, StepCompletionOutcome> {
    @Override
    protected StepCompletionOutcome doProcess(StepCompletionContext input) throws Exception {
        if (input.isExternalAbortSignalReceived()) {
            return StepCompletionOutcome.COMPLETE;
        }
        return null;
    }
}
