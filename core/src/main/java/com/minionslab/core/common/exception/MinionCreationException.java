package com.minionslab.core.common.exception;

/**
 * Exception thrown when there is an error creating a minion instance.
 */
public class MinionCreationException extends RuntimeException {

  public MinionCreationException(String message) {
    super(message);
  }

  public MinionCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
