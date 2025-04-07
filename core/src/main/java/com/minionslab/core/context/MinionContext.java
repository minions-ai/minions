package com.minionslab.core.context;

import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Simple parameters holder for minion metadatas.
 * Thread-safe and mutable to allow adding metadatas during operation execution.
 */
@Data
@Accessors(chain = true)
public class MinionContext {
    private final String contextId;
    private final String userId;
    private final String tenantId;
    private final String environmentId;
    private final Map<String, Object> metadata;

    public MinionContext(String contextId, String userId, String tenantId, String environmentId, Map<String, Object> metadata) {
        this.contextId = contextId;
        this.userId = userId;
        this.tenantId = tenantId;
        this.environmentId = environmentId;
        this.metadata = metadata;
    }



    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, T defaultValue) {
        return (T) metadata.getOrDefault(key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) {
        return (T) metadata.get(key);
    }
}
