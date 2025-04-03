package com.minionslab.core.domain;

import static com.minionslab.core.test.TestConstants.TEST_PROMPT_DESCRIPTION;
import static com.minionslab.core.test.TestConstants.TEST_PROMPT_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.minionslab.core.domain.enums.PromptType;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MinionPromptTest {

  private MinionPrompt prompt;
  private Instant now;
  private Instant future;
  private Instant past;
  private PromptComponent testComponent;

  @BeforeEach
  void setUp() {
    now = Instant.now();
    future = now.plusSeconds(3600); // 1 hour in the future
    past = now.minusSeconds(3600); // 1 hour in the past

    testComponent = PromptComponent.builder()
        .type(PromptType.SYSTEM)
        .text("Test content")
        .metadata(new HashMap<>())
        .build();

    Map<PromptType, PromptComponent> components = new HashMap<>();
    components.put(PromptType.SYSTEM, testComponent);

    prompt = MinionPrompt.builder()
        .description(TEST_PROMPT_DESCRIPTION)
        .version(TEST_PROMPT_VERSION)
        .effectiveDate(past)
        .components(components)
        .metadata(new HashMap<>())
        .build();
  }

  @Test
  void createNewVersion_ShouldCreateNewVersionWithUpdatedComponent() {
    // Create a new component
    PromptComponent newComponent = PromptComponent.builder()
        .type(PromptType.SYSTEM)
        .text("New content")
        .metadata(new HashMap<>())
        .build();

    // Create a new version
    MinionPrompt newVersion = prompt.createNewVersion(future);

    // Verify the new version
    assertEquals("1.0.1", newVersion.getVersion());
    assertEquals(future, newVersion.getEffectiveDate());
    assertEquals("New content", newVersion.getComponents().get(PromptType.SYSTEM).getText());
  }

  @Test
  void createNewVersion_WhenForceUpdateFalseAndComponentExists_ShouldThrowException() {
    // Create a new component with the same type
    PromptComponent newComponent = PromptComponent.builder()
        .type(PromptType.SYSTEM)
        .text("New content")
        .metadata(new HashMap<>())
        .build();

    // Attempt to create a new version without force update
    assertThrows(RuntimeException.class, () -> prompt.createNewVersion(future));
  }

  @Test
  void createNewVersion_WhenForceUpdateTrueAndComponentExists_ShouldUpdateComponent() {
    // Create a new component with the same type
    PromptComponent newComponent = PromptComponent.builder()
        .type(PromptType.SYSTEM)
        .text("New content")
        .metadata(new HashMap<>())
        .build();

    // Create a new version with force update
    MinionPrompt newVersion = prompt.createNewVersion(future);

    // Verify the component was updated
    assertEquals("New content", newVersion.getComponents().get(PromptType.SYSTEM).getText());
    assertEquals(future, newVersion.getEffectiveDate());
    assertEquals("1.0.1", newVersion.getVersion());
  }

  @Test
  void createNewVersion_WhenEffectiveDateInPast_ShouldSetExpiryDateOfCurrentVersion() {
    // Create a new version with past effective date
    MinionPrompt newVersion = prompt.createNewVersion(past);

    // Verify the current version's expiry date was set
    assertEquals(past, prompt.getExpiryDate());
  }

  @Test
  void createNewVersion_WhenEffectiveDateInFuture_ShouldSetExpiryDateOfCurrentVersion() {
    // Create a new version with future effective date
    MinionPrompt newVersion = prompt.createNewVersion(future);

    // Verify the current version's expiry date was set
    assertEquals(future, prompt.getExpiryDate());
  }

  @Test
  void isActiveAt_WhenNoExpiryDate_ShouldBeActive() {
    assertTrue(prompt.isActiveAt(now));
  }

  @Test
  void isActiveAt_WhenExpiryDateInFuture_ShouldBeActive() {
    prompt.setExpiryDate(future);
    assertTrue(prompt.isActiveAt(now));
  }

  @Test
  void isActiveAt_WhenExpiryDateInPast_ShouldBeInactive() {
    prompt.setExpiryDate(past);
    assertFalse(prompt.isActiveAt(now));
  }

  @Test
  void isActiveAt_WhenEffectiveDateInFuture_ShouldBeInactive() {
    prompt.setEffectiveDate(future);
    assertFalse(prompt.isActiveAt(now));
  }

  @Test
  void isActiveAt_WhenEffectiveDateInPast_ShouldBeActive() {
    prompt.setEffectiveDate(past);
    assertTrue(prompt.isActiveAt(now));
  }

  @Test
  void getStatusAt_WhenNoExpiryDate_ShouldBeActive() {
    assertEquals(EffectiveDatedEntity.Status.ACTIVE, prompt.getStatusAt(now));
  }

  @Test
  void getStatusAt_WhenExpiryDateInFuture_ShouldBeActive() {
    prompt.setExpiryDate(future);
    assertEquals(EffectiveDatedEntity.Status.ACTIVE, prompt.getStatusAt(now));
  }

  @Test
  void getStatusAt_WhenExpiryDateInPast_ShouldBeExpired() {
    prompt.setExpiryDate(past);
    assertEquals(EffectiveDatedEntity.Status.EXPIRED, prompt.getStatusAt(now));
  }

  @Test
  void getStatusAt_WhenEffectiveDateInFuture_ShouldBePendingActivation() {
    prompt.setEffectiveDate(future);
    assertEquals(EffectiveDatedEntity.Status.PENDING_ACTIVATION, prompt.getStatusAt(now));
  }

  @Test
  void getStatusAt_WhenEffectiveDateInPast_ShouldBeActive() {
    prompt.setEffectiveDate(past);
    assertEquals(EffectiveDatedEntity.Status.ACTIVE, prompt.getStatusAt(now));
  }
} 