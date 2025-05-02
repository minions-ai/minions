package com.minionslab.mcp.tool;

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
