package com.minionsai.claude.agent.memory;


import java.util.Map;

/**
 * Default implementation of the memory importance evaluator
 */
public class DefaultMemoryImportanceEvaluator implements MemoryImportanceEvaluator {

  @Override
  public double evaluateImportance(String content, MemoryType type, Map<String, Object> metadata) {
    // Simple baseline importance based on memory type
    double baseImportance = switch (type) {
      case FACT -> 0.7;
      case REFLECTION -> 0.8;
      case ERROR -> 0.6;
      case DECISION -> 0.7;
      case USER_INTERACTION -> 0.6;
      case CONVERSATION -> 0.5;
      case TOOL_USAGE -> 0.4;
      default -> 0.5;
    };

    // Adjust based on content length (longer content often more important)
    double lengthFactor = Math.min(content.length() / 500.0, 0.3);

    // Adjust based on metadata
    double metadataFactor = 0.0;
    if (metadata.containsKey("critical") && Boolean.parseBoolean(metadata.get("critical").toString())) {
      metadataFactor += 0.2;
    }
    if (metadata.containsKey("priority")) {
      String priority = metadata.get("priority").toString();
      metadataFactor += switch (priority.toLowerCase()) {
        case "high" -> 0.15;
        case "medium" -> 0.05;
        default -> 0.0;
      };
    }

    // Calculate final importance score (clamped between 0.0 and 1.0)
    return Math.min(Math.max(baseImportance + lengthFactor + metadataFactor, 0.0), 1.0);
  }
}
