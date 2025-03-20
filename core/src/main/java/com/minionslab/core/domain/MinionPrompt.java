package com.minionslab.core.domain;

import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import lombok.Builder.Default;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a system prompt with its content and metadata. This class is designed to be stored in a document database (MongoDB).
 */

@Slf4j
@Data
@Document(collection = "prompts")
@SuperBuilder
public class MinionPrompt {

    @Id
    private String id;

    @NotNull
    private String name;

    @NotNull
    private MinionType minionType;

    @NotNull
    private String version;

    @NotNull
    private String tenantId;


    @Singular
    private final Map<PromptType, PromptComponent> components;

    @Default
    private final Map<String, Object> metadata = new HashMap<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Domain behavior methods
    public void addComponent(PromptComponent component) {
        Objects.requireNonNull(component, "Component cannot be null");
        Objects.requireNonNull(component.getType(), "Component type cannot be null");
        components.put(component.getType(), component);
        updatedAt = LocalDateTime.now();
    }

    public void removeComponent(PromptType type) {
        components.remove(type);
        updatedAt = LocalDateTime.now();
    }

    public Optional<PromptComponent> getComponent(PromptType type) {
        return Optional.ofNullable(components.get(type));
    }

    public String getFullPromptText() {
        return components.values().stream()
                .sorted(Comparator.comparingDouble(PromptComponent::getOrder))
                .map(PromptComponent::getFormattedText)
                .collect(Collectors.joining("\n"));
    }

    public void updatePrompt(PromptType type, String content) {

    }

    public void addPrompt(PromptComponent component, boolean b) {
        if (components.get(component.getType()) != null && !b) {
            throw new IllegalArgumentException(String.format("Component already exists for prompt type %s", component.getType()));
        }
        addComponent(component);
    }
}