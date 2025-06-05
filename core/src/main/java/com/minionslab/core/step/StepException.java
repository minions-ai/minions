package com.minionslab.core.step;

/**
 * Custom exception for StepFactory errors.
 */
public class StepException extends RuntimeException {
    public StepException(String message) {
        super(message);
    }
    
    public StepException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static class UnknownStepTypeException extends StepException {
        public UnknownStepTypeException(String s) {
            super(s);
        }
    }
}
