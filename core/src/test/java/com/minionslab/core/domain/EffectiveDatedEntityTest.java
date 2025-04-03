package com.minionslab.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EffectiveDatedEntityTest {

  private TestEffectiveDatedEntity entity;
  private Instant now;
  private Instant future;
  private Instant past;

  @BeforeEach
  void setUp() {
    now = Instant.now();
    future = now.plusSeconds(3600); // 1 hour in the future
    past = now.minusSeconds(3600); // 1 hour in the past

    entity = TestEffectiveDatedEntity.builder()
        .effectiveDate(past)
        .build();
  }

  @Test
  void isActiveAt_WhenNoExpiryDate_ShouldBeActive() {
    assertTrue(entity.isActiveAt(now));
  }

  @Test
  void isActiveAt_WhenExpiryDateInFuture_ShouldBeActive() {
    entity.setExpiryDate(future);
    assertTrue(entity.isActiveAt(now));
  }

  @Test
  void isActiveAt_WhenExpiryDateInPast_ShouldBeInactive() {
    entity.setExpiryDate(past);
    assertFalse(entity.isActiveAt(now));
  }

  @Test
  void isActiveAt_WhenEffectiveDateInFuture_ShouldBeInactive() {
    entity.setEffectiveDate(future);
    assertFalse(entity.isActiveAt(now));
  }

  @Test
  void isActiveAt_WhenEffectiveDateInPast_ShouldBeActive() {
    entity.setEffectiveDate(past);
    assertTrue(entity.isActiveAt(now));
  }

  @Test
  void getStatusAt_WhenNoExpiryDate_ShouldBeActive() {
    assertEquals(EffectiveDatedEntity.Status.ACTIVE, entity.getStatusAt(now));
  }

  @Test
  void getStatusAt_WhenExpiryDateInFuture_ShouldBeActive() {
    entity.setExpiryDate(future);
    assertEquals(EffectiveDatedEntity.Status.ACTIVE, entity.getStatusAt(now));
  }

  @Test
  void getStatusAt_WhenExpiryDateInPast_ShouldBeExpired() {
    entity.setExpiryDate(past);
    assertEquals(EffectiveDatedEntity.Status.EXPIRED, entity.getStatusAt(now));
  }

  @Test
  void getStatusAt_WhenEffectiveDateInFuture_ShouldBePendingActivation() {
    entity.setEffectiveDate(future);
    assertEquals(EffectiveDatedEntity.Status.PENDING_ACTIVATION, entity.getStatusAt(now));
  }

  @Test
  void getStatusAt_WhenEffectiveDateInPast_ShouldBeActive() {
    entity.setEffectiveDate(past);
    assertEquals(EffectiveDatedEntity.Status.ACTIVE, entity.getStatusAt(now));
  }

  // Test class to instantiate the abstract EffectiveDatedEntity
  @SuperBuilder
  private static class TestEffectiveDatedEntity extends EffectiveDatedEntity {

    public String getId() {
      return "test-id";
    }
  }
} 