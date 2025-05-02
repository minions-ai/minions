package com.minionslab.mcp.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for language models in the MCP protocol.
 * Holds model-specific settings and parameters.
 */
@Data
@Accessors(chain = true)
@Builder
public class ModelConfig {
    @NotBlank
    private String modelId;
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    private int maxContextLength;
    private boolean streamingSupported;
    private String provider;
    private String version;
    private Double temperature;
    private Double topP;
    private int maxTokens;
    
    
    /**
     * Validates if the configuration is valid for use.
     *
     * @throws IllegalArgumentException if the configuration is invalid
     */
    public void validate() {
        if (modelId == null || modelId.isEmpty()) {
            throw new IllegalArgumentException("Model ID cannot be null or empty");
        }
        if (maxContextLength <= 0) {
            throw new IllegalArgumentException("Max context length must be positive");
        }
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("Provider cannot be null or empty");
        }
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }
    }
    
    
} 