package com.minionslab.core.message;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Data
@Accessors(chain = true)

public class DefaultMessage implements Message {
    
    private MessageRole role;
    private String content;
    
    private Instant timestamp;
    
    private Map<String, Object> metadata;
    private int tokenCount;
    
    private String id = UUID.randomUUID().toString();
    private MessageScope scope;
    
    @Builder
    public DefaultMessage(MessageScope scope, MessageRole role, String content, Map<String, Object> metadata) {
        this.role = role;
        this.content = content;
        this.metadata = metadata;
        this.scope = scope;
        this.id = UUID.randomUUID().toString();
        this.metadata = new HashMap<>();
        this.timestamp = Instant.now();
    }
    
    @Override
    public String toPromptString() {
        // Example format: [ROLE][SCOPE]: content
        StringBuilder sb = new StringBuilder();
        if (role != null) {
            sb.append("[").append(role).append("]");
        }
        if (scope != null) {
            sb.append("[").append(scope).append("]");
        }
        sb.append(": ");
        if (content != null) {
            sb.append(content);
        }
        return sb.toString();
    }

    public DefaultMessage deepCopy() {
        Map<String, Object> metadataCopy = this.metadata != null ? new HashMap<>(this.metadata) : null;
        DefaultMessage copy = new DefaultMessage(this.scope, this.role, this.content, metadataCopy);
        copy.setTimestamp(this.timestamp != null ? Instant.from(this.timestamp) : null);
        copy.setTokenCount(this.tokenCount);
        copy.setId(this.id); // preserve id for traceability
        return copy;
    }
}
