package com.minionsai.claude.communication;

/**
 * Specialized message types for common Minion interactions
 */
public enum MessageType {
  TASK_REQUEST,
  TASK_RESPONSE,
  INFORMATION_REQUEST,
  INFORMATION_RESPONSE,
  COLLABORATION_REQUEST,
  COLLABORATION_RESPONSE,
  ERROR
}