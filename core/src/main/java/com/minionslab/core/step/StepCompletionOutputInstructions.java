package com.minionslab.core.step;

import com.minionslab.core.model.OutputInstructions;

public class StepCompletionOutputInstructions extends OutputInstructions {
    private String systemMessage;
    private String schema;
    private StepCompletionInstruction outputObject;
    
    
    public  record StepCompletionInstruction(boolean isComplete, boolean isMoreCallsNeeded, String nextStepId, String failureReason, boolean isNonRecoverable) {
    }
    
}
