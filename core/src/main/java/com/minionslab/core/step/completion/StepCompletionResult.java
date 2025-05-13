package com.minionslab.core.step.completion;

public enum StepCompletionResult {
    COMPLETE,
    FAILED_STEP_DUE_TO_MAX_RETRIES,
    NON_RECOVERABLE_ERROR, PASS // pass to next link
}
