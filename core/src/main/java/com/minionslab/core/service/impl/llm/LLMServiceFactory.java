package com.minionslab.core.service.impl.llm;

import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionContextHolder;
import com.minionslab.core.service.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LLMServiceFactory {

    private final LLMService defaultLLMService;

    /**
     * Gets the appropriate LLM service based on the current context.
     * Currently returns the default service, but can be extended to support multiple providers.
     *
     * @param context The current minion context
     * @return The LLM service to use
     */
    public LLMService getLLMService(MinionContext context) {
        // TODO: Add logic to select different LLM services based on context
        // For now, return the default service
        return defaultLLMService;
    }

    /**
     * Gets the LLM service using the current context from MinionContextHolder.
     *
     * @return The LLM service to use
     */
    public LLMService getLLMService() {
        return getLLMService(MinionContextHolder.getContext());
    }
} 