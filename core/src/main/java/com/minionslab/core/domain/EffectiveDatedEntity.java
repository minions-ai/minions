package com.minionslab.core.domain;

import java.time.Instant;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for entities that need effective dating (versioning based on time).
 * This class provides common functionality for managing effective and expiry dates.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class EffectiveDatedEntity extends BaseEntity {

  @NotNull
  protected Instant effectiveDate;
  protected Instant expiryDate;

  /**
   * Checks if this entity is active at the given point in time
   */
  public boolean isActiveAt(Instant pointInTime) {
    return (expiryDate == null || expiryDate.isAfter(pointInTime)) &&
           effectiveDate.isBefore(pointInTime);
  }

  /**
   * Checks if this entity is currently active
   */
  public boolean isActive() {
    return isActiveAt(Instant.now());
  }

  /**
   * Gets the status of this entity at the given point in time
   */
  public Status getStatusAt(Instant pointInTime) {
    if (effectiveDate.isAfter(pointInTime)) {
      return Status.PENDING_ACTIVATION;
    }
    
    if (expiryDate != null && expiryDate.isBefore(pointInTime)) {
      return Status.EXPIRED;
    }
    
    return Status.ACTIVE;
  }

  /**
   * Gets the current status of this entity
   */
  public Status getStatus() {
    return getStatusAt(Instant.now());
  }

  /**
   * Represents the possible states of an effective dated entity
   */
  public enum Status {
    ACTIVE,             // Currently active and in use
    PENDING_ACTIVATION, // Created but not yet effective
    EXPIRED            // No longer active
  }
} 