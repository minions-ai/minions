package com.hls.minions.core.service.prompt;

import com.hls.minions.core.annotation.AgentPrompt;
import org.springframework.stereotype.Component;

@Component
public class AgentPromptLoader {

  private final MongoDBPromptLoader mongoDBPromptLoader;
  private final FilePromptLoader filePromptLoader;

  public AgentPromptLoader(MongoDBPromptLoader mongoDBPromptLoader, FilePromptLoader filePromptLoader) {
    this.mongoDBPromptLoader = mongoDBPromptLoader;
    this.filePromptLoader = filePromptLoader;
  }

  public String loadPrompt(Class<?> agentClass, String tenantId, String userId) {
    if (!agentClass.isAnnotationPresent(AgentPrompt.class)) {
      throw new IllegalStateException("Missing @AgentPrompt annotation for " + agentClass.getName());
    }

    AgentPrompt annotation = agentClass.getAnnotation(AgentPrompt.class);
    ScopeType scope = annotation.scope();
    SourceType source = annotation.source();

    String promptId = generatePromptId(agentClass.getName());

    // Step 1: Determine Scope Key
    String scopeKey = getScopeKey(scope, tenantId, userId);

    // Step 2: Retrieve Prompt Based on Source Type
    return switch (source) {
      case FILE -> filePromptLoader.getPrompt(scopeKey, promptId);
      case NOSQL -> mongoDBPromptLoader.getPrompt(scopeKey, promptId);
      case VECTOR -> mongoDBPromptLoader.getPrompt(scopeKey, promptId);
      default -> throw new IllegalStateException("Unexpected value: " + source);
    };
  }

  public String generatePromptId(String agentClassName) {

    // Convert CamelCase to snake_case (insert _ between words)
    String snakeCase = agentClassName.replaceAll("([a-z])([A-Z])", "$1_$2");

    // Convert to lowercase and replace non-alphabetic characters with _
    return snakeCase.toLowerCase().replaceAll("[^a-z_]", "_");
  }

  private String getScopeKey(ScopeType scope, String tenantId, String userId) {
    switch (scope) {
      case SYSTEM:
        return "system";
      case TENANT:
        return "tenant:" + tenantId;
      case USER:
        return "tenant:" + tenantId + ":user:" + userId;
      default:
        throw new IllegalStateException("Unknown scope: " + scope);
    }
  }
}
