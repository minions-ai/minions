package com.minionslab.core.model;

import com.minionslab.core.common.message.Message;

import java.util.List;
import java.util.Map;

public class Prompt {
    private final List<Message> messages;
    private final Map<String, Object> options;
    private final OutputInstructions outputInstructions;
    
    // Backward compatibility constructor
    public Prompt(List<Message> messages, Map<String, Object> options) {
        this(messages, options, null);
    }
    
    public Prompt(List<Message> messages, Map<String, Object> options, OutputInstructions outputInstructions) {
        this.messages = messages;
        this.options = options;
        this.outputInstructions = outputInstructions;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public Map<String, Object> getOptions() {
        return options;
    }
    
    public OutputInstructions getOutputInstructions() {
        return outputInstructions;
    }
} 