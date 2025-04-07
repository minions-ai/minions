package com.minionslab.core.common.exception;

public class MissingContextException extends RuntimeException {
    public MissingContextException(String message) {
        super(message);
    }

    public MissingContextException(String message, Throwable cause) {
        super(message, cause);
    }
} 