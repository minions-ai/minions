package com.minionslab.core.service.impl.llm.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class LLMResponse {
    private String requestId;
    private String promptId;
    private String promptVersion;
    private String responseText;
    private Instant timestamp;
} 