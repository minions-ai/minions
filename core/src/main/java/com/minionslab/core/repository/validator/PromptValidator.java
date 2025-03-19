package com.minionslab.core.repository.validator;

import com.minionslab.core.domain.MinionPrompt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PromptValidator {
    
    public void validate(MinionPrompt prompt) {
        if (prompt == null) {
            throw new IllegalArgumentException("Prompt cannot be null");
        }
        
        if (!StringUtils.hasText(prompt.getName())) {
            throw new IllegalArgumentException("Prompt name cannot be empty");
        }
        
        if (prompt.getType() == null) {
            throw new IllegalArgumentException("Prompt type cannot be null");
        }
        
        if (!StringUtils.hasText(prompt.getVersion())) {
            throw new IllegalArgumentException("Prompt version cannot be empty");
        }
        
        // Add more validation as needed
    }
} 