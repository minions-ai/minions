package com.hls.minions.core.agent;

import javax.naming.OperationNotSupportedException;

public class FilePromptLoader implements IPromptLoader {

  @Override public String getExactPrompt(String scopeKey) {
    return null;
  }

  @Override public String getBestMatchingPrompt(String taskDescription) throws OperationNotSupportedException {
    throw new OperationNotSupportedException("This loader does not provide best matching prompt");
  }
}
