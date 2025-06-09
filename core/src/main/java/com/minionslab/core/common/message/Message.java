package com.minionslab.core.common.message;

import com.minionslab.core.memory.strategy.MemoryItem;

import java.time.Instant;
import java.util.Map;


public interface Message extends MemoryItem {
    
    String getConversationId();
    
    String getContent();
    
    MessageRole getRole();
    
    
    MessageScope getScope();
    
    
    int getTokenCount();
    
    
    String toPromptString();
    
    /**
     * Dynamically retrieves a field value, supporting both static and derived fields.
     */
    Object getFieldValue(String field);
    
    
    Map<String, Object> getMetadata();
    
    String getId();
    
    Instant getTimestamp();
}