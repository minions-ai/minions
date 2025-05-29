package com.minionslab.core.step.completion;

import com.minionslab.core.common.chain.AbstractProcessor;

public class TimeoutProcessor extends AbstractProcessor<StepCompletionContext, StepCompletionOutcome> {
    @Override
    protected StepCompletionOutcome doProcess(StepCompletionContext input) throws Exception {
//todo figure out how to handle timeout
        return null;
    }
}
