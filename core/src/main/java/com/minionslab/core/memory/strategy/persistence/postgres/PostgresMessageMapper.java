package com.minionslab.core.memory.strategy.persistence.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.minionslab.core.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps rows from a PostgreSQL ResultSet to {@link Message} domain objects
 * and converts {@link Message} objects to an array of parameters for SQL statements.
 * Assumes metadata and embeddings are stored as JSON strings in PostgreSQL
 * (e.g., in JSONB or TEXT columns).
 */
 @Component // Uncomment if this is to be a Spring-managed bean
public class PostgresMessageMapper implements RowMapper<Message> {
    
    private static final Logger log = LoggerFactory.getLogger(PostgresMessageMapper.class);
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor.
     * If this class is managed by Spring, ObjectMapper should ideally be injected.
     * Otherwise, it's instantiated here.
     */
    // If Spring-managed:
    // @Autowired
    // public PostgresMessageMapper(ObjectMapper objectMapper) {
    //     this.objectMapper = objectMapper;
    // }
    
    // If instantiated manually:
    public PostgresMessageMapper() {
        this.objectMapper = new ObjectMapper();
        // Register JavaTimeModule if ObjectMapper might directly handle Instant to/from JSON strings.
        // For this mapper, direct Instant <-> java.sql.Timestamp conversion is used for JDBC,
        // so JavaTimeModule is less critical for the direct JDBC part but good for general ObjectMapper setup.
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
        SimpleMessage.SimpleMessageBuilder msgBuilder = SimpleMessage.builder();
        
        // ID
        msgBuilder.id(rs.getString("id"));
        
        // Timestamp (from TIMESTAMPTZ or TIMESTAMP column)
        Timestamp dbTimestamp = rs.getTimestamp("timestamp");
        if (dbTimestamp != null) {
            msgBuilder.timestamp(dbTimestamp.toInstant());
        } else {
            msgBuilder.timestamp(null); // Explicitly set to null if DB value is null
        }
        
        // Metadata (from JSONB or TEXT column)
        String metaJson = rs.getString("metadata");
        if (metaJson != null && !metaJson.trim().isEmpty()) {
            try {
                Map<String, Object> meta = objectMapper.readValue(metaJson, new TypeReference<Map<String, Object>>() {
                });
                msgBuilder.metadata(meta);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse metadata JSON for message ID {}: '{}'. Error: {}",
                        rs.getString("id"), metaJson, e.getMessage(), e);
                msgBuilder.metadata(new HashMap<>()); // Default to empty map on error
            }
        } else {
            msgBuilder.metadata(new HashMap<>());
        }
        
        // Content
        msgBuilder.content(rs.getString("content"));
        
        // Role (enum from VARCHAR or TEXT column)
        String roleStr = rs.getString("role");
        if (roleStr != null) {
            try {
                msgBuilder.role(MessageRole.valueOf(roleStr.toUpperCase())); // Robust to case
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role string '{}' for message ID {}. Setting role to null.", roleStr, rs.getString("id"));
                msgBuilder.role(null); // Or handle with a default role if appropriate
            }
        }
        
        // Scope (enum from VARCHAR or TEXT column)
        String scopeStr = rs.getString("scope");
        if (scopeStr != null) {
            try {
                msgBuilder.scope(MessageScope.valueOf(scopeStr.toUpperCase())); // Robust to case
            } catch (IllegalArgumentException e) {
                log.warn("Invalid scope string '{}' for message ID {}. Setting scope to null.", scopeStr, rs.getString("id"));
                msgBuilder.scope(null); // Or handle with a default scope
            }
        }
        
        // Token Count
        msgBuilder.tokenCount(rs.getInt("token_count"));
        
        // Embedding (from JSONB or TEXT column storing a JSON array of numbers)
        //todo handle EmbeddingMessage
/*        String embJson = rs.getString("embedding");
        if (embJson != null && !embJson.trim().isEmpty()) {
            try {
                List<Number> embList = objectMapper.readValue(embJson, new TypeReference<List<Number>>() {
                });
                if (embList != null) {
                    float[] emb = new float[embList.size()];
                    for (int i = 0; i < embList.size(); i++) {
                        emb[i] = embList.get(i).floatValue();
                    }
                    msgBuilder.setEmbedding(emb);
                } else {
                    msgBuilder.setEmbedding(null);
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to parse embedding JSON for message ID {}: '{}'. Error: {}",
                        rs.getString("id"), embJson, e.getMessage(), e);
                msgBuilder.setEmbedding(null); // Default to null on error
            }
        } else {
            msgBuilder.setEmbedding(null);
        }*/
        
        return msgBuilder.build();
    }
    
    /**
     * Converts a {@link Message} domain object into an array of objects suitable
     * for use as parameters in a PreparedStatement.
     * The order of parameters in the returned array MUST match the order of
     * '?' placeholders in the target SQL query.
     *
     * @param message The Message object to map.
     * @return An array of objects representing SQL parameters.
     * @throws RuntimeException if JSON serialization of metadata or embedding fails.
     */
    public Object[] toSqlParams(Message message) {
        if (message == null) {
            // Or throw IllegalArgumentException, depending on how you want to handle this.
            // Returning nulls might lead to SQL errors if columns are NOT NULL.
            log.warn("Attempting to map a null Message to SQL parameters. This will likely result in an array of nulls.");
            return new Object[8]; // Assuming 8 parameters, all will be null
        }
        
        String metaJson = null;
        if (message.getMetadata() != null && !message.getMetadata().isEmpty()) {
            try {
                metaJson = objectMapper.writeValueAsString(message.getMetadata());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize metadata to JSON for message ID {}: {}", message.getId(), e.getMessage(), e);
                throw new RuntimeException("Failed to serialize metadata for Message ID: " + message.getId(), e);
            }
        }
        
        String embJson = null;
        if (message instanceof EmbeddingMessage) {
            float[] embedding = ((EmbeddingMessage) message).getEmbedding(); // Direct method call
            if (embedding != null && embedding.length > 0) {
                try {
                    embJson = objectMapper.writeValueAsString(embedding);
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize embedding to JSON for message ID {}: {}", message.getId(), e.getMessage(), e);
                    throw new RuntimeException("Failed to serialize embedding for Message ID: " + message.getId(), e);
                }
            }
        }
        
        return new Object[]{
                message.getId(),
                message.getTimestamp() != null ? Timestamp.from(message.getTimestamp()) : null, // Convert Instant to java.sql.Timestamp
                metaJson,
                message.getContent(),
                message.getRole() != null ? message.getRole().name() : null,
                message.getScope() != null ? message.getScope().name() : null,
                message.getTokenCount(), // Primitives like int are fine
                embJson
        };
    }
}