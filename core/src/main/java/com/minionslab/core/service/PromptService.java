package com.minionslab.core.service;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public interface PromptService {

  /**
   * Creates a new prompt with the given components.
   *
   * @param entityId The unique identifier for the prompt
   * @param description The description of the prompt
   * @param version The version of the prompt
   * @param components The components of the prompt
   * @param metadata Additional metadata for the prompt
   * @param effectiveDate The date when this prompt should become effective
   * @param expiryDate The date when this prompt should expire
   * @return The created prompt
   */
  @Transactional
  MinionPrompt createPrompt(
      @NotBlank String entityId,
      @NotBlank String description,
      @NotBlank String version,
      @NotNull Map<PromptType, PromptComponent> components,
      Map<String, Object> metadata,
      Instant effectiveDate,
      Instant expiryDate);

  /**
   * Gets the active version of a prompt at the given point in time.
   *
   * @param entityId The ID of the prompt to retrieve
   * @param pointInTime The point in time to check
   * @return The active prompt at the given point in time
   */
  MinionPrompt getActiveVersionAt(String entityId, Instant pointInTime);

  /**
   * Gets the active version of a prompt at the current time.
   *
   * @param promptId The ID of the prompt to retrieve
   * @return The currently active prompt
   */
  MinionPrompt getActiveVersionAt(String promptId);

  /**
   * Gets all versions of a prompt.
   *
   * @param entityId The ID of the prompt to retrieve
   * @return List of all versions of the prompt
   */
  List<MinionPrompt> getAllVersions(String entityId);

  /**
   * Gets a specific version of a prompt by entity ID and version.
   *
   * @param entityId The entity ID of the prompt
   * @param version The version to retrieve
   * @return The prompt if found
   */
  Optional<MinionPrompt> getPromptByEntityIdAndVersion(String entityId, String version);



  /**
   * Gets a prompt by its ID.
   *
   * @param promptId The ID of the prompt
   * @return The prompt if found
   */
  Optional<MinionPrompt> getPrompt(@NotBlank String promptId);

  /**
   * Updates an existing prompt.
   *
   * @param promptId The ID of the prompt to update
   * @param description The new description
   * @param components The new components
   * @param metadata The new metadata
   * @param effectiveDate The new effective date
   * @param expiryDate The new expiry date
   * @param incrementVersionIfNeeded Whether to increment the version if the prompt is locked
   * @return The updated prompt
   */
  @Transactional
  MinionPrompt updatePrompt(
      String promptId,
      String description,
      Map<PromptType, PromptComponent> components,
      Map<String, Object> metadata,
      Instant effectiveDate,
      Instant expiryDate,
      boolean incrementVersionIfNeeded);

  /**
   * Saves a prompt.
   *
   * @param prompt The prompt to save
   * @return The saved prompt
   */
  MinionPrompt savePrompt(MinionPrompt prompt);

  /**
   * Gets all prompts.
   *
   * @return List of all prompts
   */
  List<MinionPrompt> getPrompts();

  /**
   * Gets a prompt by its entity ID.
   *
   * @param entityId The entity ID of the prompt
   * @return The prompt if found
   */
  Optional<MinionPrompt> getPromptByEntityId(String entityId);

  /**
   * Deletes a prompt.
   *
   * @param promptId The ID of the prompt to delete
   */
  void deletePrompt(String promptId);

  /**
   * Updates a component of a prompt.
   *
   * @param promptId The ID of the prompt
   * @param updateEffectiveDate The new effective date
   * @param componentType The type of component to update
   * @param componentText The new component text
   * @param componentMetadata The new component metadata
   * @param incrementVersionIfNeeded Whether to increment the version if the prompt is locked
   * @return The updated prompt
   */
  @Transactional
  MinionPrompt updateComponent(
      String promptId,
      Instant updateEffectiveDate,
      PromptType componentType,
      String componentText,
      Map<String, Object> componentMetadata,
      boolean incrementVersionIfNeeded);
}
