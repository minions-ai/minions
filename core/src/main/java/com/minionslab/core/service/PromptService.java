package com.minionslab.core.service;

import com.minionslab.core.common.exception.PromptException;
import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionContextHolder;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.repository.PromptRepository;
import com.minionslab.core.repository.validator.PromptValidator;
import com.minionslab.core.service.resolver.PromptResolver;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service interface for managing system prompts
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PromptService {

  private final PromptRepository promptRepository;
  private final PromptValidator promptValidator;
  private final PromptResolver promptResolver;
  private final MinionContext minionContext = MinionContextHolder.getContext();

  /**
   * Save a system prompt with all its components
   */
  public MinionPrompt savePrompt(MinionPrompt prompt) {
    validatePrompt(prompt);

    // Check for duplicate version
    if (promptRepository.existsByTypeAndNameAndVersionAndTenantId(
        prompt.getType(),
        prompt.getName(),
        prompt.getVersion(),
        prompt.getTenantId())) {
      throw new PromptException.DuplicatePromptException(
          "Prompt already exists with version: " + prompt.getVersion());
    }

    return promptRepository.save(prompt);
  }

  /**
   * Validate required fields for a prompt
   */
  public void validateRequiredFields(MinionPrompt prompt) {
    if (prompt.getType() == null) {
      throw new IllegalStateException("Agent minionType is required");
    }
    if (!StringUtils.hasText(prompt.getName())) {
      throw new IllegalStateException("Name is required");
    }
  }

  /**
   * Get a system prompt by ID
   */
  public Optional<MinionPrompt> getPrompts(String promptId) {
    return promptRepository.findById(promptId);
  }

  /**
   * Get all system prompts for a tenant
   */
  public List<MinionPrompt> getPrompts() {
    return promptRepository.findAllByTenantId(null);
  }

  /**
   * Get all system prompts for a tenant and minionType
   */
  public List<MinionPrompt> getPrompts(MinionType type) {
    return promptRepository.findAllByTenantId(null).stream()
        .filter(prompt -> prompt.getType().equals(type))
        .collect(Collectors.toList());
  }

  /**
   * Get the latest prompt for a minion minionType and name
   */
  public Optional<MinionPrompt> getPrompt(MinionType type, String name) {
    return promptRepository.findLatestByTypeAndNameAndTenantId(type, name, minionContext.getTenantId());
  }

  /**
   * Get a user-defined prompt by name and promptVersion
   */
  public Optional<MinionPrompt> getPrompt(String promptName, String promptVersion) {
    return promptRepository.findByNameAndVersionAndTenantId(promptName, promptVersion, minionContext.getTenantId());
  }

  public List<MinionPrompt> getPrompts(MinionType agentType, String promptName) {
    return promptRepository.findAllByTenantId(null).stream()
        .filter(prompt -> prompt.getType().equals(agentType) && prompt.getName().equals(promptName))
        .collect(Collectors.toList());
  }

  public Optional<MinionPrompt> getPrompts(MinionType minionType, String promptName, String version) {
    return promptRepository.findByTypeAndNameAndVersionAndTenantId(minionType, promptName, version, minionContext.getTenantId());
  }

  public Optional<MinionPrompt> getPromptForAgentType(
      MinionType agentType,
      String promptName,
      String version,
      String tenantId) {

    if (version != null) {
      return promptRepository.findByTypeAndNameAndVersionAndTenantId(
          agentType, promptName, version, tenantId);
    }

    return promptRepository.findLatestByTypeAndNameAndTenantId(
        agentType, promptName, tenantId);
  }

  public List<MinionPrompt> getPromptsByType(MinionType type, String tenantId) {
    return promptRepository.findAllByTypeAndTenantId(type, tenantId);
  }

  public Optional<MinionPrompt> getLatestPrompt(
      MinionType type,
      String name,
      String tenantId) {
    return promptRepository.findLatestByTypeAndNameAndTenantId(type, name, tenantId);
  }

  public List<MinionPrompt> getAllPrompts(String tenantId) {
    return promptRepository.findAllByTenantId(tenantId);
  }

  /**
   * Get a specific prompt by its unique identifier
   */
  public Optional<MinionPrompt> getPromptById(String id) {
    return promptRepository.findById(id);
  }

  /**
   * Delete a prompt by its ID
   */
  public void deletePrompt(String id) {
    if (!promptRepository.findById(id).isPresent()) {
      throw new PromptException.PromptNotFoundException("Prompt not found: " + id);
    }
    promptRepository.deleteById(id);
  }

  private void validatePrompt(MinionPrompt prompt) {
    if (prompt == null) {
      throw new PromptException.InvalidPromptException("Prompt cannot be null");
    }
    if (!StringUtils.hasText(prompt.getName())) {
      throw new PromptException.InvalidPromptException("Prompt name cannot be empty");
    }
    if (prompt.getType() == null) {
      throw new PromptException.InvalidPromptException("Prompt type cannot be null");
    }
    if (!StringUtils.hasText(prompt.getVersion())) {
      throw new PromptException.InvalidPromptException("Prompt version cannot be empty");
    }
    if (!StringUtils.hasText(prompt.getTenantId())) {
      throw new PromptException.InvalidPromptException("Tenant ID cannot be empty");
    }
  }


}
