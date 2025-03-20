package com.minionslab.core.service.resolver;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Default resolver that provides a fallback prompt when no other resolver succeeds.
 */
@Slf4j

public class DefaultResolver extends BasePromptResolver {

  @Override
  public Optional<MinionPrompt> resolve(PromptResolutionContext context) {
    log.info("Using default prompt for minion: {}", context.getName());

    // Create a default prompt
    MinionPrompt defaultPrompt = MinionPrompt.builder()
        .name(context.getName())
        .type(context.getMinionType())
        .version(context.getVersion())
        .build();



    return Optional.of(defaultPrompt);
  }
} 