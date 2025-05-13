package com.minionslab.core.message;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a message in the Model Context Protocol.
 * Messages are the fundamental unit of communication in MCP.
 */
public interface Message {
    
    
    String getId();
    
    MessageRole getRole();
    
    String getContent();
    
    Instant getTimestamp();
    
    Map<String, Object> getMetadata();
    
    
    int getTokenCount();
    
    MessageScope getScope();
    
    String toPromptString();
} 