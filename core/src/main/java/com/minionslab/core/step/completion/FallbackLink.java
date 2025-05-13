package com.minionslab.core.step.completion;

import com.minionslab.core.step.StepExecution;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FallbackLink implements StepCompletionLink {
    @Override
    public StepCompletionResult check(StepExecution execution) {
        log.warn("StepCompletionChain fallback: No completion condition met for step {}", execution.getStep() != null ? execution.getStep().getId() : "unknown");
        return StepCompletionResult.FAILED_STEP_DUE_TO_MAX_RETRIES;
    }
} 