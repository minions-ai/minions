package com.minionslab.core.service.resolver;

import com.minionslab.core.domain.MinionPrompt;
import java.util.Optional;

/**
 * Interface for prompt resolution strategies.
 * Uses Chain of Responsibility pattern to allow multiple resolution strategies.
 */
public interface PromptResolver {
    /**
     * Attempts to resolve a prompt based on the resolution parameters
     * @param context The resolution parameters containing all necessary information
     * @return Optional containing the resolved prompt, or empty if not resolved
     */
    Optional<MinionPrompt> resolve(PromptResolutionContext context);
    
    /**
     * Sets the next resolver in the chain
     * @param next The next resolver
     * @return The next resolver
     */
    PromptResolver setNext(PromptResolver next);
} 