package com.minionslab.persistence.chroma;

import com.minionslab.core.memory.MemoryQuery;
import com.minionslab.core.memory.strategy.PersistenceAdapter;
import com.minionslab.core.message.Message;
import lombok.RequiredArgsConstructor;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ChromaPersistenceAdapter implements PersistenceAdapter {

    private final ChromaClient chromaClient;

    @Override
    public void save(Message message) {
        float[] embedding = extractEmbedding(message);
        chromaClient.save(message, embedding);
    }

    @Override
    public List<Message> query(MemoryQuery query, Map<String, Object> extraParams) {
        // If the context or message contains an embedding, do a vector search
        float[] embedding = extractEmbeddingFromParams(extraParams);
        Integer topK = extractTopKFromParams(extraParams);
        List<Message> results;
        if (embedding != null && topK != null && topK > 0) {
            results = chromaClient.vectorSearch(embedding, topK);
        } else {
            // Otherwise, filter by metadata, role, scope, etc.
            List<Message> all = chromaClient.findAll();
            results = all.stream()
                .filter(m -> query.getRole() == null || m.getRole() == query.getRole())
                .filter(m -> query.getScope() == null || m.getScope() == query.getScope())
                .filter(m -> query.getKeyword() == null || m.getContent().toLowerCase().contains(query.getKeyword().toLowerCase()))
                .filter(m -> {
                    if (query.getMetadata() == null || query.getMetadata().isEmpty()) return true;
                    Map<String, Object> meta = m.getMetadata();
                    return query.getMetadata().entrySet().stream()
                        .allMatch(e -> meta != null && e.getValue().equals(meta.get(e.getKey())));
                })
                .filter(m -> {
                    if (query.getAfter() != null && m.getTimestamp() != null && m.getTimestamp().isBefore(query.getAfter())) return false;
                    if (query.getBefore() != null && m.getTimestamp() != null && m.getTimestamp().isAfter(query.getBefore())) return false;
                    return true;
                })
                .filter(m -> query.getEntityType() == null || 
                    (m.getMetadata() != null && query.getEntityType().equals(m.getMetadata().get("entityType"))))
                .sorted(Comparator.comparing(Message::getTimestamp).reversed())
                .limit(query.getLimit() > 0 ? query.getLimit() : Long.MAX_VALUE)
                .collect(Collectors.toList());
        }
        return results;
    }

    // Helper to extract embedding from a message (customize as needed)
    private float[] extractEmbedding(Message message) {
        Object embeddingObj = message.getMetadata() != null ? message.getMetadata().get("embedding") : null;
        if (embeddingObj instanceof float[]) {
            return (float[]) embeddingObj;
        }
        return null;
    }

    // Helper to extract embedding from extraParams (customize as needed)
    private float[] extractEmbeddingFromParams(Map<String, Object> params) {
        if (params != null && params.containsKey("embedding")) {
            Object embeddingObj = params.get("embedding");
            if (embeddingObj instanceof float[]) {
                return (float[]) embeddingObj;
            }
        }
        return null;
    }

    // Helper to extract topK from extraParams (customize as needed)
    private Integer extractTopKFromParams(Map<String, Object> params) {
        if (params != null && params.containsKey("topK")) {
            Object topKObj = params.get("topK");
            if (topKObj instanceof Integer) {
                return (Integer) topKObj;
            }
        }
        return null;
    }
} 