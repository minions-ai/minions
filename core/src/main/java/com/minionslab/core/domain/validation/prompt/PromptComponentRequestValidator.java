package com.minionslab.core.domain.validation.prompt;

import com.minionslab.core.api.dto.PromptComponentRequest;
import com.minionslab.core.domain.enums.PromptType;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Map;

public class PromptComponentRequestValidator implements Validator {
    private static final int MAX_COMPONENT_LENGTH = 10000; // 10KB

    @Override
    public boolean supports(Class<?> clazz) {
        return PromptComponentRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PromptComponentRequest component = (PromptComponentRequest) target;
        
        // Validate component type
        if (component.getType() == null) {
            errors.rejectValue("type", "prompt.component.type.null", 
                "Component type cannot be null");
        }

        // Validate component content
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "content", 
            "prompt.component.content.empty", "Component content cannot be empty");

        if (component.getContent() != null && component.getContent().length() > MAX_COMPONENT_LENGTH) {
            errors.rejectValue("content", "prompt.component.content.tooLong", 
                "Component content exceeds maximum length");
        }

        // Validate component metadata
        validateComponentMetadata(component.getMetadatas(), errors);
    }

    private void validateComponentMetadata(Map<String, Object> metadata, Errors errors) {
        if (metadata != null) {
            metadata.forEach((key, value) -> {
                if (key == null || key.trim().isEmpty()) {
                    errors.rejectValue("metadata", "prompt.component.metadata.key.empty", 
                        "Component metadata key cannot be empty");
                }
                if (value == null) {
                    errors.rejectValue("metadata", "prompt.component.metadata.value.null", 
                        "Component metadata value cannot be null for key: " + key);
                }
            });
        }
    }
}
