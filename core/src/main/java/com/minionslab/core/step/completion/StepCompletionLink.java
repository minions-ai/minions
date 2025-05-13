package com.minionslab.core.step.completion;

import com.minionslab.core.step.StepExecution;

public interface StepCompletionLink {
    StepCompletionResult check(StepExecution execution);
}
