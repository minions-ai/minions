package com.minionslab.core.common.exception;

public class MinionException extends RuntimeException {
    
    public MinionException(String message) {
        super(message);
    }

    public MinionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class MinionNotFoundException extends MinionException {
        public MinionNotFoundException(String minionId) {
            super(String.format("Minion not found with ID: %s", minionId));
        }
    }

    public static class InvalidMinionIdException extends MinionException {
        public InvalidMinionIdException(String message) {
            super(message);
        }
    }

    public static class CreationException extends MinionException {

        public CreationException(String s, Throwable e) {
            super(s, e);
        }
    }

    public static class ContextCreationException extends MinionException {


        public ContextCreationException(String failedToCreateMinionContext, Exception e) {
            super(failedToCreateMinionContext, e);
        }

        public ContextCreationException(String contextCreationFailed) {
            super(contextCreationFailed);
        }
    }

    public static class ContextNotFoundException extends MinionException {

        public ContextNotFoundException(String noContextFound) {
            super(noContextFound);
        }
    }

    public static class ContextMismatchException extends MinionException {

        public ContextMismatchException(String s) {
            super(s);
        }
    }

    public static class PromptNotFoundException extends MinionException {

        public PromptNotFoundException(String s) {
            super(s);
        }
    }

    public static class ProcessingException extends MinionException {

        public ProcessingException(String failedToProcessRequest, Exception e) {
            super(failedToProcessRequest, e);
        }
    }
}