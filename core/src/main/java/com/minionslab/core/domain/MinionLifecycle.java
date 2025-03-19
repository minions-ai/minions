package com.minionslab.core.domain;

import com.minionslab.core.domain.enums.MinionState;

/**
 * Interface defining the lifecycle methods for a minion.
 */
public interface MinionLifecycle {
  /**
   * Initialize the minion with required components and configurations
   */
  void initialize();

  /**
   * Start the minion's operations
   */
  void start();

  /**
   * Pause the minion's operations
   */
  void pause();

  /**
   * Resume the minion's operations
   */
  void resume();

  /**
   * Shutdown the minion gracefully
   */
  void shutdown();

  /**
   * Handle a failure in the minion
   */
  void handleFailure(Exception error);

  /**
   * Get the current state of the minion
   */
  MinionState getState();
}