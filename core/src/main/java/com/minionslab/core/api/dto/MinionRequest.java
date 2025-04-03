package com.minionslab.core.api.dto;

import com.minionslab.core.domain.enums.MinionType;
import java.time.Instant;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MinionRequest {
    private String userId;
    private String tenantId;
    @NotNull(message = "User prompt is required")
    private String userPrompt;    // The actual user prompt/request
    private Map<String, Object> parameters; // Additional parameters for the prompt

}