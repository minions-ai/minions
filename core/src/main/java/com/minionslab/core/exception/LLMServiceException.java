package com.minionslab.core.exception;

public class LLMServiceException extends RuntimeException {
    
    public LLMServiceException(String message) {
        super(message);
    }

    public LLMServiceException(String message, Throwable cause) {
        super(message, cause);
    }
} 