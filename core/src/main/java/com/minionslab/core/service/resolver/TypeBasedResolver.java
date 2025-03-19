package com.minionslab.core.service.resolver;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.service.PromptService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Resolves prompts based on minion minionType, name, and version.
 */
@Slf4j
@RequiredArgsConstructor
public class TypeBasedResolver extends BasePromptResolver {

  @Qualifier("filePromptService") private final PromptService promptService;

  @Override
  public Optional<MinionPrompt> resolve(PromptResolutionContext context) {
    try {
      // Try to resolve by minionType, name, and version
      Optional<MinionPrompt> prompt = context.getVersion() != null
          ? promptService.getPrompts(context.getMinionType(), context.getName(), context.getVersion())
          : promptService.getPrompt(context.getMinionType(), context.getName());

      if (prompt.isPresent()) {
        log.debug("Resolved prompt by minionType: {} for minion: {}", context.getMinionType(), context.getName());
        return prompt;
      }

      // Continue to next resolver if not found
      log.debug("No prompt found by minionType, trying next resolver");
      return resolveNext(context);
    } catch (Exception e) {
      log.error("Error resolving prompt by minionType for minion: {}", context.getName(), e);
      return resolveNext(context);
    }
  }
} 