package com.minionslab.core.memory;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(String format) {
        super(format);
    }
}
