package com.minionslab.core.api.dto;

import com.minionslab.core.domain.enums.MinionType;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MinionRequest {
    private String userId;
    private String tenantId;
    @NotNull(message = "Prompt name is required")
    private String promptName;    // Name of the prompt
    @NotNull(message = "Version is required")
    private String version;       // Optional version
    @NotNull(message = "Minion type is required")
    private MinionType minionType;
    @NotNull(message = "User prompt is required")
    private String userPrompt;    // The actual user prompt/request
    private Map<String, Object> parameters; // Additional parameters for the prompt



    // Getters and Setters
} 