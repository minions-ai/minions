package com.minionslab.core.exception;

public class ToolException extends RuntimeException {

  public ToolException(String s) {
    super(s);
  }

  public static class ToolNotAvailableException extends ToolException {
    public ToolNotAvailableException(String s) {
      super(s);
    }
  }
}
