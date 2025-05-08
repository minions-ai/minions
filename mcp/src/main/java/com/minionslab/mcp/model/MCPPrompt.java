package com.minionslab.mcp.model;

import com.minionslab.mcp.message.MCPMessage;
import java.util.List;
import java.util.Map;

public class MCPPrompt {
    private final List<MCPMessage> messages;
    private final Map<String, Object> options;

    public MCPPrompt(List<MCPMessage> messages, Map<String, Object> options) {
        this.messages = messages;
        this.options = options;
    }

    public List<MCPMessage> getMessages() {
        return messages;
    }

    public Map<String, Object> getOptions() {
        return options;
    }
} 