package com.minionslab.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minionslab.core.domain.enums.PromptType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a single component/section within a system prompt.
 * This class provides JSON formatting capabilities for prompt content.
 */
@Data 
@Accessors(chain = true) 
@SuperBuilder
@Slf4j
public class PromptComponent extends BaseEntity {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Builder.Default 
    private String id = UUID.randomUUID().toString();

    private String content;

    /**
     * Embedding vector ID for this component (when stored in vector DB)
     */
    private String embeddingId;

    /**
     * Weight of this component (used for vector search)
     */
    @Builder.Default 
    private double weight = 1.0;

    /**
     * Order in which this component should appear (lower values first)
     */
    @Builder.Default 
    private double order = 0.0;

    /**
     * Component type (e.g., "instruction", "parameters", "examples", etc.)
     */
    @Builder.Default 
    private PromptType type = PromptType.DYNAMIC;

    /**
     * Additional metadata for this component
     */
    @Builder.Default 
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Returns the component content in JSON format.
     *
     * @return JSON string representation of the component
     */
    @JsonIgnore
    public String getJsonContent() {
        try {
            ObjectNode node = objectMapper.createObjectNode()
                .put("id", id)
                .put("type", type.name())
                .put("content", Objects.requireNonNullElse(content, ""))
                .put("order", order)
                .put("weight", weight);

            if (embeddingId != null) {
                node.put("embeddingId", embeddingId);
            }

            if (!metadata.isEmpty()) {
                node.set("metadata", objectMapper.valueToTree(metadata));
            }

            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize PromptComponent to JSON", e);
            return String.format("{\"error\":\"Failed to serialize component %s\"}", id);
        }
    }

    /**
     * Returns the formatted prompt text with XML-style tags.
     *
     * @return Formatted prompt text
     */
    @JsonIgnore
    public String getFullPromptText() {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }

        StringBuilder formatted = new StringBuilder();
        
        // Add section header based on component type
        if (type != null) {
            formatted.append('<').append(type.name()).append('>').append('\n');
        }

        // Add the main content
        formatted.append(content.trim()).append('\n');

        // Close the section
        if (type != null) {
            formatted.append("</").append(type.name()).append('>').append('\n');
        }

        return formatted.toString();
    }

    /**
     * Returns the raw content without formatting.
     *
     * @return Raw content string
     */
    @JsonIgnore
    public String getRawContent() {
        return Objects.requireNonNullElse(content, "").trim();
    }

    /**
     * Adds a metadata entry to this component.
     *
     * @param key Metadata key
     * @param value Metadata value
     * @throws IllegalArgumentException if key is null
     */
    public void addMetadata(String key, Object value) {
        Objects.requireNonNull(key, "Metadata key cannot be null");
        metadata.put(key, value);
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * @return Unmodifiable metadata map
     */
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }


}
