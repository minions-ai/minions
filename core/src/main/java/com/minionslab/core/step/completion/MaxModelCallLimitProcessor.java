package com.minionslab.core.step.completion;


import com.minionslab.core.common.chain.AbstractProcessor;
import com.minionslab.core.model.ModelCallStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MaxModelCallLimitProcessor extends AbstractProcessor<StepCompletionContext, StepCompletionOutcome> {
    
    // Use correct Spring default value syntax
    @Value("${maxModelCallLimit:10}")
    private int maxModelCallLimit;
    
    /**
     * Marks the step as complete if the number of non-pending model calls
     * reaches the configured limit.
     */
    @Override
    protected StepCompletionOutcome doProcess(StepCompletionContext input) {
        int limit = input.getMaxModelCallLimit() > 0 ? input.getMaxModelCallLimit() : maxModelCallLimit;
        
        // Defensive: handle possible nulls
        if (input.getStepContext() == null || input.getStepContext().getModelCalls() == null) {
            return null;
        }
        
        long completedCount = input.getStepContext().getModelCalls().stream()
                                   .filter(modelCall -> modelCall.getStatus() != ModelCallStatus.PENDING)
                                   .count();
        
        return completedCount >= limit ? StepCompletionOutcome.COMPLETE : null;
    }
}
