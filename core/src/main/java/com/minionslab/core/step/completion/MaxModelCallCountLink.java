package com.minionslab.core.step.completion;

import com.minionslab.core.step.StepExecution;

public class MaxModelCallCountLink implements StepCompletionLink {
    private final int maxModelCalls;
    public MaxModelCallCountLink(int maxModelCalls) { this.maxModelCalls = maxModelCalls; }
    @Override
    public StepCompletionResult check(StepExecution execution) {
        if (execution.getModelCalls().size() > maxModelCalls) {
            return StepCompletionResult.FAILED_STEP_DUE_TO_MAX_RETRIES;
        }
        return StepCompletionResult.PASS;
    }
} 