package com.minionslab.core.model;

import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class OutputInstructions {
    private List<String> systemMessages; // e.g., "You must output JSON matching this schema"
    private String schema; // JSON schema as string
    private Object outputObject; // Could be a Class, Map, or other structure
    
    
    public OutputInstructions(List<String> systemMessages, String schema, Object outputObject) {
        this.systemMessages = systemMessages;
        this.schema = schema;
        this.outputObject = outputObject;
    }
    
    public List<String> getSystemMessages() {
        return systemMessages;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public Object getOutputObject() {
        return outputObject;
    }
}
