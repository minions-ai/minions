package com.minionslab.api.test.controller.dto;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.MinionType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PromptRequest {
    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Type is required")
    private MinionType type;

    @NotNull(message = "Version is required")
    @Pattern(regexp = "^\\d+\\.\\d+(\\.\\d+)?$", message = "Version must be in format X.Y or X.Y.Z")
    private String version;

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "Components are required")
    private List<PromptComponent> components = new ArrayList<>();

    private Map<String, Object> metadata = new HashMap<>();

    public MinionPrompt toMinionPrompt() {
        return MinionPrompt.builder()

                .version(version)
                .tenantId(tenantId)
                .metadata(metadata)
                .build();
    }
} 