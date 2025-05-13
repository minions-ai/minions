package com.minionslab.core.model;

import org.springframework.ai.model.Model;

public record ModelInfo(
    String name,
    String type,
    String provider,
    String modelId,
    Model model
) {
    public static ModelInfo from(String beanName, Model model) {
        String className = model.getClass().getSimpleName();
        // Try to extract provider and modelId from className or beanName heuristically
        String provider = extractProvider(beanName, className);
        String modelId = extractModelId(beanName, className);
        String type = model.getClass().getInterfaces().length > 0 ? model.getClass().getInterfaces()[0].getSimpleName() : "Unknown";
        return new ModelInfo(beanName, type, provider, modelId, model);
    }

    private static String extractProvider(String beanName, String className) {
        String lower = beanName.toLowerCase() + " " + className.toLowerCase();
        if (lower.contains("openai")) return "openai";
        if (lower.contains("anthropic")) return "anthropic";
        if (lower.contains("ollama")) return "ollama";
        if (lower.contains("mistral")) return "mistral";
        if (lower.contains("huggingface")) return "huggingface";
        if (lower.contains("vertexai")) return "vertexai";
        // Add more providers as needed
        return "unknown";
    }

    private static String extractModelId(String beanName, String className) {
        String lower = beanName.toLowerCase() + " " + className.toLowerCase();
        // OpenAI naming conventions
        if (lower.contains("openaiaudiospeechmodel")) return "audioSpeech";
        if (lower.contains("openaiaudiotranscriptionmodel")) return "audioTranscription";
        if (lower.contains("openaichatmodel")) return "chat";
        if (lower.contains("openaienbeddingmodel")) return "embedding";
        if (lower.contains("openaimoderationmodel")) return "moderation";
        if (lower.contains("openaiimagemodel")) return "image";
        // Fallbacks for other providers
        if (lower.contains("gpt-4o")) return "gpt-4o";
        if (lower.contains("gpt-4")) return "gpt-4";
        if (lower.contains("gpt-3")) return "gpt-3";
        if (lower.contains("claude-3")) return "claude-3";
        if (lower.contains("llama2")) return "llama2";
        // Add more model ids as needed
        return className;
    }
} 