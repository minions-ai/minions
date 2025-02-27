package com.minionsai.claude.ai.impl;

/**
 * Exception thrown when AI service operations fail.
 */
public class AIServiceException extends RuntimeException {
  public AIServiceException(String message) {
    super(message);
  }

  public AIServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}