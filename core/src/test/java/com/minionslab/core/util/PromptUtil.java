package com.minionslab.core.util;

import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PromptUtil {
    public static @NotNull PromptComponent getPromptComponent(PromptType type, String text) {
        return PromptComponent.builder()
                .type(type)
                .text(text)
                .build();
    }

    public static @NotNull PromptComponent getPromptComponent(String text) {
        return getPromptComponent(PromptType.DYNAMIC, text);
    }

    public static @NotNull Map.Entry<PromptType, PromptComponent> getPromptComponentEntry(PromptType type, String promptText) {
        PromptComponent promptComponent = getPromptComponent(type, promptText);
        return Map.entry(promptComponent.getType(), promptComponent);
    }
}
