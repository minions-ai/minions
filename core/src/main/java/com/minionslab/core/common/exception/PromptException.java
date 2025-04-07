package com.minionslab.core.common.exception;

public class PromptException extends RuntimeException {

  public PromptException(String message) {
    super(message);
  }

  public PromptException(String message, Throwable cause) {
    super(message, cause);
  }

  public static class PromptNotFoundException extends PromptException {

    public PromptNotFoundException(String message) {
      super(message);
    }
  }

  public static class InvalidPromptException extends PromptException {

    public InvalidPromptException(String message) {
      super(message);
    }
  }

  public static class DuplicatePromptException extends PromptException {

    public DuplicatePromptException(String s) {
      super(s);
    }
  }

  public static class InvalidPromptIdException extends PromptException {

    public InvalidPromptIdException(String s) {
      super(s);
    }
  }

  public static class PromptIsLockedException extends PromptException {

    public PromptIsLockedException(String s) {
      super(s);
    }
  }
}