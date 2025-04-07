package com.minionslab.core.service.resolver;

import com.minionslab.core.service.PromptResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating and configuring the chain of prompt resolvers.
 * This class should only be accessed through PromptResolverService.
 * Direct instantiation or injection is not allowed.
 */
@Slf4j
@RequiredArgsConstructor public class PromptResolverChainFactory {

    /**
     * Creates and configures the chain of prompt resolvers.
     *
     * @return The first resolver in the chain
     */
    public PromptResolver createResolverChain() {
        log.debug("Creating prompt resolver chain");

        // Chain resolvers in order of precedence
        PromptResolver resolver = new DefaultResolver();

        return resolver;
    }
} 