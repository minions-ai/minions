package com.minionslab.core.domain.tools.exception;

/**
 * Exception thrown when there are issues during tool registration
 */
public class ToolRegistrationException extends ToolException {

  public ToolRegistrationException(String toolName) {
    super(String.format("Failed to register tool: %s", toolName));
  }

  public ToolRegistrationException(String toolName, Throwable cause) {
    super(String.format("Failed to register tool: %s", toolName), cause);
  }


} 