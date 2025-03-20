package com.minionslab.core.api.dto;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.MinionType;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PromptResponse {

    private String id;
    private String name;
    private MinionType type;
    private String version;
    private String tenantId;
    @Singular
    private List<PromptComponent> contents;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;

    public static PromptResponse fromMinionPrompt(MinionPrompt prompt) {
        return PromptResponse.builder()
                .id(prompt.getId())
                .name(prompt.getName())
                .type(prompt.getMinionType())
                .version(prompt.getVersion())
                .tenantId(prompt.getTenantId())
                .contents(prompt.getComponents().values())
                .metadata(prompt.getMetadata())
                .createdAt(prompt.getCreatedAt().toInstant(ZoneOffset.UTC))
                .updatedAt(prompt.getUpdatedAt().toInstant(ZoneOffset.UTC))
                .build();
    }
} 