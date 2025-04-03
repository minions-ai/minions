package com.minionslab.core.service.resolver;


import com.minionslab.core.service.PromptResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Factory for creating and configuring the chain of prompt resolvers.
 */
@Slf4j @Configuration @RequiredArgsConstructor public class PromptResolverChainFactory {



  /**
   * Creates and configures the chain of prompt resolvers.
   *
   * @return The first resolver in the chain
   */
  @Bean public PromptResolver createResolverChain() {
    log.debug("Creating prompt resolver chain");

    // Chain resolvers in order of precedence
    PromptResolver resolver = new DefaultResolver();

    return resolver;
  }
} 