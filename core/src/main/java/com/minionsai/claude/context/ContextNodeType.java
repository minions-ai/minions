package com.minionsai.claude.context;

/**
 * Enumerates the different types of context nodes
 */
public enum ContextNodeType {
  GLOBAL,     // Global context at the root
  TASK,       // Task context
  SUBTASK,    // Subtask context
  AGENT,      // Agent-specific context
  TEMPORARY   // Temporary context for short-lived operations
}
