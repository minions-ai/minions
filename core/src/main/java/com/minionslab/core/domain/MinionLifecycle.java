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
   * Stop the minion's operations
   */
  void stop();

  /**
   * Pause the minion's operations
   */
  void pause();

  /**
   * Resume the minion's operations from idle state
   */
  void resume();

  /**
   * Resume the minion's operations from waiting state to processing state
   */
  void resumeProcessing();

  /**
   * Pause the minion's operations during processing
   */
  void pauseProcessing();

  /**
   * Recover the minion from error state to idle state
   */
  void recover();

  /**
   * Reinitialize the minion from error state
   */
  void reinitialize();

  /**
   * Start processing from started state
   */
  void startProcessing();

  /**
   * Shutdown the minion gracefully
   */
  void shutdown();

  /**
   * Handle a failure in the minion's operations
   *
   * @param error the exception that caused the failure
   */
  void handleFailure(Exception error);

  /**
   * Get the current state of the minion
   *
   * @return the current state
   */
  MinionState getState();
}