package com.minionslab.core.domain;

import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
/*
// todo: Created a hybrid approach to recipes. Keep a base hardcoded and then ability to retrieve more recipes from the DB
*/

@Data
@Builder
public class MinionRecipe {
    private final MinionType type;
    private final Set<PromptType> requiredComponents;
    private final Set<String> requiredMetadata;
    private final Map<String, Object> defaultMetadata;
    private final String description;
    private final boolean requiresTenant;
    private final List<ChateMemoryStrategy> memoryStrategies = new ArrayList<>();
    private final Set<String> requiredToolboxes;  // Set of required toolbox names

    public void validatePrompt(MinionPrompt prompt) {
        if (prompt == null) {
            throw new IllegalArgumentException("Prompt cannot be null");
        }

        // Validate required components
        for (PromptType requiredType : requiredComponents) {
            if (!prompt.getComponents().containsKey(requiredType)) {
                throw new MinionException.InvalidPromptException(
                    String.format("Missing required component type: %s for minion type: %s", 
                        requiredType, type));
            }
        }

        // Validate required metadata
        if (requiredMetadata != null) {
            for (String requiredKey : requiredMetadata) {
                if (!prompt.getMetadata().containsKey(requiredKey)) {
                    throw new MinionException.InvalidPromptException(
                        String.format("Missing required metadata key: %s for minion type: %s", 
                            requiredKey, type));
                }
            }
        }

        // Validate required toolboxes
        if (requiredToolboxes != null && !requiredToolboxes.isEmpty()) {
            Set<String> promptToolboxes = prompt.getToolboxes();
            if (promptToolboxes == null || promptToolboxes.isEmpty()) {
                throw new MinionException.InvalidPromptException(
                    String.format("Missing required toolboxes for minion type: %s", type));
            }

            for (String requiredToolbox : requiredToolboxes) {
                if (!promptToolboxes.contains(requiredToolbox)) {
                    throw new MinionException.InvalidPromptException(
                        String.format("Missing required toolbox: %s for minion type: %s", 
                            requiredToolbox, type));
                }
            }
        }
    }
} 