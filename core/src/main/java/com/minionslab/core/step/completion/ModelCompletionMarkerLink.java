package com.minionslab.core.step.completion;

import com.minionslab.core.step.StepCompletionOutputInstructions.StepCompletionInstruction;
import com.minionslab.core.step.StepExecution;

import java.util.regex.Pattern;

public class ModelCompletionMarkerLink implements StepCompletionLink {
    private static final Pattern COMPLETION_PATTERN = Pattern.compile("\\b(status|step_status|next_step)\\s*[:=]\\s*(completed|done|success)", Pattern.CASE_INSENSITIVE);
    
    @Override
    public StepCompletionResult check(StepExecution execution) {
        // Check for StepCompletionInstruction in model call responses
        StepCompletionInstruction completionResult = execution.getCompletionResult();
        if (completionResult.isComplete()) {
            return StepCompletionResult.COMPLETE;
        } else if (completionResult.isNonRecoverable()) {
            return StepCompletionResult.NON_RECOVERABLE_ERROR;
        }
        return StepCompletionResult.PASS;
    }
}