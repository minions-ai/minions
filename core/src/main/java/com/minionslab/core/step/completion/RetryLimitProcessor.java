package com.minionslab.core.step.completion;

import com.minionslab.core.common.chain.AbstractProcessor;
import org.springframework.stereotype.Component;

@Component
public class RetryLimitProcessor extends AbstractProcessor<StepCompletionContext, StepCompletionOutcome> {
    @Override
    protected StepCompletionOutcome doProcess(StepCompletionContext input) throws Exception {
        //todo retry limit processor
        return null;
    }
}
