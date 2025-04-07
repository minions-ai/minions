package com.minionslab.core.service.resolver;

import com.minionslab.core.domain.MinionPrompt;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Default resolver that provides a fallback prompt when no other resolver succeeds.
 */
@Slf4j

public class DefaultResolver extends BasePromptResolver {

  @Override
  public Optional<MinionPrompt> resolve(PromptResolutionContext context) {
    log.info("Using default prompt for minion: {}", context.getDescription());

    // Create a default prompt
    MinionPrompt defaultPrompt = MinionPrompt.builder()
        .description(context.getDescription())
        .version(context.getVersion())
        .build();

    return Optional.of(defaultPrompt);
  }
} 