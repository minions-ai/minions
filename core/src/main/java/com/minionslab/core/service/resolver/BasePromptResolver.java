package com.minionslab.core.service.resolver;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Base implementation of PromptResolver that provides chain of responsibility functionality.
 */
@Slf4j
public abstract class BasePromptResolver implements PromptResolver {
    protected PromptResolver nextResolver;
    
    @Override
    public PromptResolver setNext(PromptResolver next) {
        this.nextResolver = next;
        return next;
    }
    
    /**
     * Helper method to pass resolution to the next resolver in the chain
     * @param context The resolution parameters
     * @return Optional containing the resolved prompt from the next resolver, or empty if no next resolver
     */
    protected Optional<MinionPrompt> resolveNext(PromptResolutionContext context) {
        if (nextResolver != null) {
            return nextResolver.resolve(context);
        }
        return Optional.empty();
    }
} 