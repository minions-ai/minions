package com.minionsai.claude.exceptions;

public class ToolNotFoundException extends Exception {
  public ToolNotFoundException(String toolId) {
    super("Tool not found: " + toolId);
  }
}