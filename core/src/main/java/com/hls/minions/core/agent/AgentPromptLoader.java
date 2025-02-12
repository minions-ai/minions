package com.hls.minions.core.agent;


import javax.naming.OperationNotSupportedException;

public class AgentPromptLoader {

  private final MongoDBPromptLoader mongoDBPromptLoader;
  private final FilePromptLoader filePromptLoader;

  public AgentPromptLoader(MongoDBPromptLoader mongoDBPromptLoader) {
    this.mongoDBPromptLoader = mongoDBPromptLoader;
    this.filePromptLoader = new FilePromptLoader();
  }

  public String loadPrompt(Class<?> agentClass, String tenantId, String userId) throws OperationNotSupportedException {
    if (!agentClass.isAnnotationPresent(AgentPrompt.class)) {
      throw new IllegalStateException("Missing @AgentPrompt annotation for " + agentClass.getName());
    }

    AgentPrompt annotation = agentClass.getAnnotation(AgentPrompt.class);
    ScopeType scope = annotation.scope();
    SourceType source = annotation.source();

    // Step 1: Determine Scope Key
    String scopeKey = getScopeKey(scope, tenantId, userId);

    // Step 2: Retrieve Prompt Based on Source Type
    switch (source) {
      case MONGO_DB -> mongoDBPromptLoader.getExactPrompt(scopeKey);
      case FILE -> filePromptLoader.getExactPrompt(scopeKey);
    }
    if (source == SourceType.MONGO_DB) {
      return mongoDBPromptLoader.getExactPrompt(scopeKey);
    } else if (source == SourceType.FILE) {
      return mongoDBPromptLoader.getBestMatchingPrompt(annotation.value());
    }

    throw new RuntimeException("Unsupported source type: " + source);
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
