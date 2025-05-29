package com.minionslab.core.step.completion;

import com.minionslab.core.common.chain.AbstractProcessor;

public class UnrecoverableErrorProcessor extends AbstractProcessor<StepCompletionContext, StepCompletionOutcome> {
    @Override
    protected StepCompletionOutcome doProcess(StepCompletionContext input) throws Exception {
        Throwable error = input.getError();
        if (error != null && error.getMessage().contains("Unrecoverable error")) {
            throw new RuntimeException(error);
        }
        return null;
    }
}
