package com.minionslab.core.domain.enums;

/**
 * Represents the possible states of a minion.
 */
public enum MinionState {
    CREATED,
    INITIALIZING,
    IDLE,
    PROCESSING,
    WAITING,
    ERROR,
    SHUTTING_DOWN,
    SHUTDOWN
} 