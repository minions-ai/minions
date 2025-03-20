package com.minionslab.core.api.dto;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.PromptType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class UpdatePromptRequest {

    @NotNull(message = "Content is required")
    private String content;

    @NotNull
    private MinionPrompt minionPrompt;

    private PromptType type = PromptType.DYNAMIC;

    private Map<String, Object> metadata;

    public MinionPrompt updateMinionPrompt(MinionPrompt existingPrompt) {
        minionPrompt.updatePrompt(type, content);
        return minionPrompt;

    }
} 