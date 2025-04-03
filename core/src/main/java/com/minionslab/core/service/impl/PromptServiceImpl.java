package com.minionslab.core.service.impl;

import com.minionslab.core.api.dto.CreatePromptRequest;
import com.minionslab.core.api.dto.PromptComponentRequest;
import com.minionslab.core.api.dto.PromptResponse;
import com.minionslab.core.api.dto.UpdatePromptRequest;
import com.minionslab.core.common.exception.PromptException;
import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionContextHolder;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.repository.PromptRepository;
import com.minionslab.core.service.PromptService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

/**
 * Service interface for managing system prompts
 */
@Service
@RequiredArgsConstructor
@Validated
public class PromptServiceImpl implements PromptService {

  private static final Logger log = LoggerFactory.getLogger(PromptServiceImpl.class);


  private final PromptRepository promptRepository;

  @Autowired
  private Validator validator;


  /**
   * Creates a new prompt with the given components.
   *
   * @param request The request containing the prompt details
   * @return The created prompt
   */
  @Transactional @Override public MinionPrompt createPrompt(@Valid CreatePromptRequest request) {
    try {

      MinionPrompt minionPrompt = request.toMinionPrompt();

      return promptRepository.save(minionPrompt);
    } catch (PromptException e) {
      log.error("Failed to create prompt: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error while creating prompt", e);
      throw new PromptException("Failed to create prompt: " + e.getMessage());
    }
  }


  /**
   * Gets the active version of a prompt at the given point in time.
   *
   * @param entityId    The ID of the prompt to retrieve
   * @param pointInTime The point in time to check
   * @return The active prompt at the given point in time
   */
  @Override public PromptResponse getActiveVersion(String entityId, Instant pointInTime) {
    MinionPrompt prompt = promptRepository.findActiveVersion(entityId, pointInTime, getCurrentTenantId())
        .orElseThrow(() -> new PromptException("No active version found for prompt: " + entityId));
    return convertToResponse(prompt);
  }

  /**
   * Gets the active version of a prompt at the current time.
   *
   * @param promptId The ID of the prompt to retrieve
   * @return The currently active prompt
   */
  @Override public PromptResponse getActiveVersion(String promptId) {
    return getActiveVersion(promptId, Instant.now());
  }

  /**
   * Gets all versions of a prompt.
   *
   * @param entityId The ID of the prompt to retrieve
   * @return List of all versions of the prompt
   */
  @Override public List<PromptResponse> getAllVersions(String entityId) {
    return promptRepository.findAllVersions(entityId, getCurrentTenantId())
        .stream()
        .map(this::convertToResponse)
        .toList();
  }

  private String getCurrentTenantId() {
    MinionContext context = MinionContextHolder.getContext();

    return context.getTenantId();
  }


  @Override public Optional<MinionPrompt> getPromptByEntityIdAndVersion(String name, String version) {

    return promptRepository.findByEntityIdAndVersionAndTenantId(name, version, getCurrentTenantId());
  }

  @Override public Optional<MinionPrompt> getActivePromptAt(String entityId, Instant effectiveDate) {

    return promptRepository.findActiveVersion(entityId, effectiveDate, getCurrentTenantId());
  }

  /**
   * Converts a MinionPrompt to a PromptResponse
   */
  private PromptResponse convertToResponse(MinionPrompt prompt) {
    return PromptResponse.fromMinionPrompt(prompt);
  }


  @Override public Optional<MinionPrompt> getPrompt(String promptId) {
    return promptRepository.findById(promptId);
  }

  @Transactional @Override public PromptResponse updatePrompt(String promptId, UpdatePromptRequest request,
      boolean incrementVersionIfNeeded) {
    log.info("Updating prompt: {}", promptId);

    Instant updatedEffectiveDate = request.getEffectiveDate() != null ? request.getEffectiveDate() : Instant.now();
    MinionPrompt currentPrompt = getPromptForUpdate(promptId, updatedEffectiveDate, incrementVersionIfNeeded);

    currentPrompt = request.updateMinionPrompt(currentPrompt);

    return convertToResponse(promptRepository.save(currentPrompt));
  }

  private MinionPrompt getPromptForUpdate(String promptId, Instant updateEffectiveDate, boolean incrementVersionIfNeeded) {
    MinionPrompt prompt = getPrompt(promptId).orElseThrow(
        () -> new PromptException.PromptNotFoundException("No active version found for prompt: " + promptId));

    if (prompt.isLocked()) {
      if (incrementVersionIfNeeded) {
        prompt = prompt.createNewVersion(updateEffectiveDate);
      } else {
        throw new PromptException.PromptIsLockedException(
            "Cannot update component without version increment. Set the incrementVersionIfNeeded parameter to true. Promot id: "
                + promptId);
      }
    }
    return prompt;
  }


  @Override public MinionPrompt savePrompt(MinionPrompt samplePrompt) {
    return promptRepository.save(samplePrompt);
  }

  @Override public List<MinionPrompt> getPrompts() {
    List<MinionPrompt> allByTenantId = promptRepository.findAllByTenantId(getCurrentTenantId());
    return allByTenantId;
  }

  @Override public Optional<MinionPrompt> getPromptByEntityId(String entityId) {
    return promptRepository.findLatestByEntityIdAndTenantId(entityId, getCurrentTenantId());
  }

  @Override public void deletePrompt(String promptId) {
    promptRepository.deleteById(promptId);
  }

  @Transactional @Override public PromptResponse updateComponent(String promptId, Instant updateEffectiveDate,
      PromptComponentRequest request,
      boolean incrementVersionIfNeeded) {
    log.info("Updating component {} for prompt: {}", request.getType(), promptId);
    MinionPrompt currentPrompt = getPromptForUpdate(promptId, updateEffectiveDate, incrementVersionIfNeeded);

    PromptComponent newComponent = PromptComponent.builder()
        .type(request.getType())
        .text(request.getContent())
        .metadata(request.getMetadatas())
        .build();

    currentPrompt.getComponents().put(newComponent.getType(), newComponent);
    return convertToResponse(promptRepository.save(currentPrompt));
  }
}
