package com.minionslab.core.model;

import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;

import java.time.Instant;
import java.util.Map;

public class EntityMessage implements Message {
    @Override
    public String getId() {
        return "";
    }
    
    @Override
    public MessageRole getRole() {
        return null;
    }
    
    @Override
    public String getContent() {
        return "";
    }
    
    @Override
    public Instant getTimestamp() {
        return null;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return Map.of();
    }
    
    @Override
    public int getTokenCount() {
        return 0;
    }
    
    @Override
    public MessageScope getScope() {
        return null;
    }
    
    @Override
    public String toPromptString() {
        return "";
    }

}
