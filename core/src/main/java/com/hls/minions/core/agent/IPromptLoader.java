package com.hls.minions.core.agent;

import javax.naming.OperationNotSupportedException;

public interface IPromptLoader {

  String getExactPrompt(String scopeKey);

  String getBestMatchingPrompt(String taskDescription) throws OperationNotSupportedException;
}
