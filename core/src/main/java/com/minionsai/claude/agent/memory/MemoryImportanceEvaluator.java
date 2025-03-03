package com.minionsai.claude.agent.memory;


import java.util.Map;

/**
 * Interface for evaluating memory importance
 */
public interface MemoryImportanceEvaluator {

  double evaluateImportance(String content, MemoryType type, Map<String, Object> metadata);
}
