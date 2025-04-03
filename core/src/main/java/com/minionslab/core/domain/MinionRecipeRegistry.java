package com.minionslab.core.domain;

import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class MinionRecipeRegistry {
    private final Map<MinionType, MinionRecipe> recipes = new HashMap<>();

    @PostConstruct
    public void init() {
        // Initialize with known recipes
        recipes.put(MinionType.USER_DEFINED_AGENT, MinionRecipe.builder()
            .type(MinionType.USER_DEFINED_AGENT)
            .requiredComponents(Set.of(PromptType.SYSTEM, PromptType.USER_TEMPLATE))
            .requiredMetadata(Set.of("model"))
            .defaultMetadata(Map.of("temperature", 0.7))
            .description("A user-defined agent with custom behavior")
            .requiresTenant(true)
            .build());

        recipes.put(MinionType.COMMUNICATION_AGENT, MinionRecipe.builder()
            .type(MinionType.COMMUNICATION_AGENT)
            .requiredComponents(Set.of(PromptType.SYSTEM, PromptType.PERSONA, PromptType.USER_TEMPLATE))
            .requiredMetadata(Set.of("model"))
            .defaultMetadata(Map.of("temperature", 0.7))
            .description("A communication agent that handles user interactions")
            .requiresTenant(true)
            .build());
    }

    public MinionRecipe getRecipe(MinionType type) {
        MinionRecipe recipe = recipes.getOrDefault(type,recipes.get(MinionType.USER_DEFINED_AGENT));
        if (recipe == null) {
            throw new MinionException.RecipeNotFoundException("No recipe found for type: " + type);
        }
        return recipe;
    }

    public boolean hasRecipe(MinionType type) {
        return recipes.containsKey(type);
    }

    public void registerRecipe(MinionRecipe recipe) {
        recipes.put(recipe.getType(), recipe);
    }

    public void validatePrompt(MinionType type, MinionPrompt prompt) {
        MinionRecipe recipe = getRecipe(type);
        recipe.validatePrompt(prompt);
    }

    public Map<String, Object> getDefaultMetadata(MinionType type) {
        MinionRecipe recipe = getRecipe(type);
        return recipe.getDefaultMetadata();
    }

    public boolean requiresTenant(MinionType type) {
        MinionRecipe recipe = getRecipe(type);
        return recipe.isRequiresTenant();
    }
} 