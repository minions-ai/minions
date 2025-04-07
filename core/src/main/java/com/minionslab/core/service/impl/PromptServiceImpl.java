package com.minionslab.core.service.impl;

import com.minionslab.core.common.exception.PromptException;
import com.minionslab.core.common.util.ContextUtils;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.repository.PromptRepository;
import com.minionslab.core.service.PromptService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

/**
 * Service implementation for managing system prompts
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Validated
public class PromptServiceImpl implements PromptService {

  private final PromptRepository promptRepository;
  private final Validator validator;

  /**
   * Creates a new prompt with the given components.
   *
   * @param entityId      The unique identifier for the prompt
   * @param description   The description of the prompt
   * @param version       The version of the prompt
   * @param components    The components of the prompt
   * @param metadata      Additional metadata for the prompt
   * @param effectiveDate The date when this prompt should become effective
   * @param expiryDate    The date when this prompt should expire
   * @return The created prompt
   */
  @Override
  @Transactional
  public MinionPrompt createPrompt(
      @NotBlank String entityId,
      @NotBlank String description,
      @NotBlank String version,
      @NotNull Map<PromptType, PromptComponent> components,
      Map<String, Object> metadata,
      Instant effectiveDate,
      Instant expiryDate) {
    if (entityId == null || entityId.isBlank()) {
      throw new PromptException("Entity ID cannot be blank");
    }
    if (description == null || description.isBlank()) {
      throw new PromptException("Description cannot be blank");
    }
    if (version == null || version.isBlank()) {
      throw new PromptException("Version cannot be blank");
    }

    String tenantId = ContextUtils.getRequiredTenantId();

    MinionPrompt prompt = MinionPrompt.builder()
        .entityId(entityId)
        .description(description)
        .version(version)
        .components(components)
        .metadata(metadata)
        .effectiveDate(effectiveDate)
        .expiryDate(expiryDate)
        .tenantId(tenantId)
        .build();

    return promptRepository.save(prompt);
  }

  /**
   * Gets the active version of a prompt at the given point in time.
   *
   * @param entityId    The ID of the prompt to retrieve
   * @param pointInTime The point in time to check
   * @return The active prompt at the given point in time
   */
  @Override
  @Transactional(readOnly = true)
  public MinionPrompt getActiveVersionAt(@NotBlank String entityId, Instant pointInTime) {

    String tenantId = ContextUtils.getRequiredTenantId();

    return promptRepository.findActiveVersion(entityId, pointInTime)
        .orElseThrow(() -> new PromptException("No active version found for prompt: " + entityId));
  }

  /**
   * Gets the active version of a prompt at the current time.
   *
   * @param promptId The ID of the prompt to retrieve
   * @return The currently active prompt
   */
  @Override
  public MinionPrompt getActiveVersionAt(@NotBlank String promptId) {
    return getActiveVersionAt(promptId, Instant.now());
  }

  /**
   * Gets all versions of a prompt.
   *
   * @param entityId The ID of the prompt to retrieve
   * @return List of all versions of the prompt
   */
  @Override
  @Transactional(readOnly = true)
  public List<MinionPrompt> getAllVersions(@NotBlank String entityId) {

    String tenantId = ContextUtils.getRequiredTenantId();

    return promptRepository.findAllVersions(entityId);
  }


  @Override public Optional<MinionPrompt> getPrompt(String promptId) {
    return promptRepository.findById(promptId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<MinionPrompt> getPromptByEntityIdAndVersion(String entityId, String version) {
    if (entityId == null || entityId.isBlank()) {
      throw new PromptException("Entity ID cannot be blank");
    }
    if (version == null || version.isBlank()) {
      throw new PromptException("Version cannot be blank");
    }

    String tenantId = ContextUtils.getRequiredTenantId();

    return promptRepository.findByEntityIdAndVersion(entityId, version);
  }

  /**
   * Updates an existing prompt.
   *
   * @param promptId                 The ID of the prompt to update
   * @param description              The new description
   * @param components               The new components
   * @param metadata                 The new metadata
   * @param effectiveDate            The new effective date
   * @param expiryDate               The new expiry date
   * @param incrementVersionIfNeeded Whether to increment the version if the prompt is locked
   * @return The updated prompt
   */
  @Override
  @Transactional
  public MinionPrompt updatePrompt(
      String promptId,
      String description,
      Map<PromptType, PromptComponent> components,
      Map<String, Object> metadata,
      Instant effectiveDate,
      Instant expiryDate,
      boolean incrementVersionIfNeeded) {
    if (promptId == null || promptId.isBlank()) {
      throw new PromptException("Prompt ID cannot be blank");
    }

    String tenantId = ContextUtils.getRequiredTenantId();

    MinionPrompt currentPrompt = promptRepository.findById(promptId)
        .orElseThrow(() -> new PromptException("Prompt not found"));

    if (currentPrompt.isLocked()) {
      if (!incrementVersionIfNeeded) {
        throw new PromptException("Prompt is locked and cannot be updated");
      }
      currentPrompt.setExpiryDate(effectiveDate);
      promptRepository.save(currentPrompt);
      currentPrompt = currentPrompt.createNewVersion(effectiveDate);
    }

    if (description != null && !description.isBlank()) {
      currentPrompt.setDescription(description);
    }
    if (components != null && !components.isEmpty()) {
      currentPrompt.setComponents(components);
    }
    if (metadata != null) {
      currentPrompt.setMetadata(metadata);
    }
    if (effectiveDate != null) {
      currentPrompt.setEffectiveDate(effectiveDate);
    }
    if (expiryDate != null) {
      currentPrompt.setExpiryDate(expiryDate);
    }

    return promptRepository.save(currentPrompt);
  }

  @Override
  public MinionPrompt savePrompt(@Valid MinionPrompt prompt) {
    return promptRepository.save(prompt);
  }

  @Override
  public List<MinionPrompt> getPrompts() {

    return promptRepository.findAll();
  }

  @Override
  public Optional<MinionPrompt> getPromptByEntityId(String entityId) {
    String tenantId = ContextUtils.getRequiredTenantId();
    return promptRepository.findLatestByEntityId(entityId);
  }

  @Override
  public void deletePrompt(String promptId) {
    String tenantId = ContextUtils.getRequiredTenantId();
    promptRepository.deleteById(promptId);
  }

  /**
   * Updates a component of a prompt.
   *
   * @param promptId                 The ID of the prompt
   * @param updateEffectiveDate      The new effective date
   * @param componentType            The type of component to update
   * @param componentText            The new component text
   * @param componentMetadata        The new component metadata
   * @param incrementVersionIfNeeded Whether to increment the version if the prompt is locked
   * @return The updated prompt
   */
  @Override
  @Transactional
  public MinionPrompt updateComponent(
      String promptId,
      Instant updateEffectiveDate,
      PromptType componentType,
      String componentText,
      Map<String, Object> componentMetadata,
      boolean incrementVersionIfNeeded) {
    if (promptId == null || promptId.isBlank()) {
      throw new PromptException("Prompt ID cannot be blank");
    }

    String tenantId = ContextUtils.getRequiredTenantId();

    MinionPrompt currentPrompt = promptRepository.findById(promptId)
        .orElseThrow(() -> new PromptException("Prompt not found"));

    PromptComponent newComponent = PromptComponent.builder()
        .type(componentType)
        .text(componentText)
        .metadata(componentMetadata != null ? componentMetadata : Map.of())
        .build();

    currentPrompt.getComponents().put(newComponent.getType(), newComponent);
    return promptRepository.save(currentPrompt);
  }
}
