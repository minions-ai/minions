package com.minionslab.core.service;

import com.minionslab.core.api.dto.CreatePromptRequest;
import com.minionslab.core.api.dto.UpdatePromptRequest;
import com.minionslab.core.common.exception.PromptException;
import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionContextHolder;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.repository.PromptRepository;
import com.minionslab.core.repository.validator.PromptValidator;
import com.minionslab.core.service.resolver.PromptResolver;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Service interface for managing system prompts
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PromptService {

  private final PromptRepository repository;
  private final PromptValidator validator;
  private final PromptResolver promptResolver;
  private final MinionContext minionContext = MinionContextHolder.getContext();

  @Transactional
  public MinionPrompt createPrompt(CreatePromptRequest request) {
    validator.validate(request);
    
    MinionPrompt prompt = MinionPrompt.builder()
        .name(request.getName())
        .minionType(request.getType())
        .version(request.getVersion())
        .tenantId(request.getTenantId())
        .metadata(request.getMetadata())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
        
    request.getComponents().forEach(prompt::addComponent);
    
    return repository.save(prompt);
  }
  
  @Transactional
  public MinionPrompt updatePrompt(String id, UpdatePromptRequest request) {
    MinionPrompt prompt = findPromptOrThrow(id);
    
    prompt.setName(request.getName());
    prompt.setMinionType(request.getMinionType());
    prompt.setVersion(request.getVersion());
    prompt.setMetadata(request.getMetadata());
    
    // Update components
    prompt.getComponents().clear();
    request.getComponents().forEach(prompt::addComponent);
    
    return repository.save(prompt);
  }
  
  @Transactional
  public MinionPrompt addComponent(String promptId, PromptComponent component) {
    MinionPrompt prompt = findPromptOrThrow(promptId);
    prompt.addComponent(component);
    return repository.save(prompt);
  }
  
  @Transactional
  public MinionPrompt updateComponent(String promptId, PromptType type, String text) {
    MinionPrompt prompt = findPromptOrThrow(promptId);
    
    PromptComponent component = PromptComponent.builder()
        .type(type)
        .text(text)
        .build();
        
    prompt.addComponent(component);
    return repository.save(prompt);
  }
  
  @Transactional
  public void deletePrompt(String id) {
    if (!repository.existsById(id)) {
      throw new PromptException.PromptNotFoundException(id);
    }
    repository.deleteById(id);
  }
  
  // Query methods
  public Optional<MinionPrompt> findById(String id) {
    return repository.findById(id);
  }
  
  public List<MinionPrompt> findByMinionType(MinionType type) {
    return repository.findByMinionTypeAndTenantId(type, getCurrentTenantId());
  }
  
  public Optional<MinionPrompt> findLatestByTypeAndName(MinionType type, String name) {
    return repository.findLatestByTypeAndNameAndTenantId(type, name, getCurrentTenantId());
  }
  
  public List<MinionPrompt> findAllByTenantId() {
    return repository.findAllByTenantId(getCurrentTenantId());
  }
  
  private MinionPrompt findPromptOrThrow(String id) {
    return repository.findById(id)
        .orElseThrow(() -> new PromptException.PromptNotFoundException(id));
  }
  
  private String getCurrentTenantId() {
    return SecurityContextHolder.getContext().getTenantId();
  }

  /**
   * Save a system prompt with all its components
   */
  public MinionPrompt savePrompt(MinionPrompt prompt) {
    validatePrompt(prompt);

    // Check for duplicate version
    if (repository.existsByTypeAndNameAndVersionAndTenantId(
        prompt.getType(),
        prompt.getName(),
        prompt.getVersion(),
        prompt.getTenantId())) {
      throw new PromptException.DuplicatePromptException(
          "Prompt already exists with version: " + prompt.getVersion());
    }

    return repository.save(prompt);
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
    return repository.findById(promptId);
  }

  /**
   * Get all system prompts for a tenant
   */
  public List<MinionPrompt> getPrompts() {
    return repository.findAllByTenantId(null);
  }

  /**
   * Get all system prompts for a tenant and minionType
   */
  public List<MinionPrompt> getPrompts(MinionType type) {
    return repository.findAllByTenantId(null).stream()
        .filter(prompt -> prompt.getType().equals(type))
        .collect(Collectors.toList());
  }

  /**
   * Get the latest prompt for a minion minionType and name
   */
  public Optional<MinionPrompt> getPrompt(MinionType type, String name) {
    return repository.findLatestByTypeAndNameAndTenantId(type, name, minionContext.getTenantId());
  }

  /**
   * Get a user-defined prompt by name and promptVersion
   */
  public Optional<MinionPrompt> getPrompt(String promptName, String promptVersion) {
    return repository.findByNameAndVersionAndTenantId(promptName, promptVersion, minionContext.getTenantId());
  }

  public List<MinionPrompt> getPrompts(MinionType agentType, String promptName) {
    return repository.findAllByTenantId(null).stream()
        .filter(prompt -> prompt.getType().equals(agentType) && prompt.getName().equals(promptName))
        .collect(Collectors.toList());
  }

  public Optional<MinionPrompt> getPrompts(MinionType minionType, String promptName, String version) {
    return repository.findByTypeAndNameAndVersionAndTenantId(minionType, promptName, version, minionContext.getTenantId());
  }

  public Optional<MinionPrompt> getPromptForAgentType(
      MinionType agentType,
      String promptName,
      String version,
      String tenantId) {

    if (version != null) {
      return repository.findByTypeAndNameAndVersionAndTenantId(
          agentType, promptName, version, tenantId);
    }

    return repository.findLatestByTypeAndNameAndTenantId(
        agentType, promptName, tenantId);
  }

  public List<MinionPrompt> getPromptsByType(MinionType type, String tenantId) {
    return repository.findAllByTypeAndTenantId(type, tenantId);
  }

  public Optional<MinionPrompt> getLatestPrompt(
      MinionType type,
      String name,
      String tenantId) {
    return repository.findLatestByTypeAndNameAndTenantId(type, name, tenantId);
  }

  public List<MinionPrompt> getAllPrompts(String tenantId) {
    return repository.findAllByTenantId(tenantId);
  }

  /**
   * Get a specific prompt by its unique identifier
   */
  public Optional<MinionPrompt> getPromptById(String id) {
    return repository.findById(id);
  }

  /**
   * Adds content to the DYNAMIC component of a prompt
   */
  public MinionPrompt addContent(String promptId, String content) {
    MinionPrompt prompt = getPromptOrThrow(promptId);
    
    PromptComponent dynamicComponent = prompt.getComponents().computeIfAbsent(
        PromptType.DYNAMIC,
        type -> PromptComponent.builder()
            .type(type)
            .content("")
            .build()
    );
    
    dynamicComponent.appendContent(content);
    return repository.save(prompt);
  }

  /**
   * Adds or updates a component in a prompt
   */
  public MinionPrompt addPromptPart(String promptId, PromptType type, String content, boolean overwrite) {
    MinionPrompt prompt = getPromptOrThrow(promptId);
    
    if (prompt.getComponents().containsKey(type) && !overwrite) {
        throw new IllegalStateException(
            String.format("Component of type %s already exists and overwrite is false", type)
        );
    }

    PromptComponent component = PromptComponent.builder()
        .type(type)
        .content(content)
        .build();
        
    prompt.getComponents().put(type, component);
    return repository.save(prompt);
  }

  /**
   * Updates an existing component in a prompt
   */
  public MinionPrompt updatePromptPart(String promptId, PromptType type, String content) {
    MinionPrompt prompt = getPromptOrThrow(promptId);
    
    PromptComponent component = prompt.getComponents().get(type);
    if (component == null) {
        throw new IllegalStateException(
            String.format("No component found of type %s", type)
        );
    }

    component.setContent(content);
    return repository.save(prompt);
  }

  /**
   * Updates multiple components in a prompt
   */
  public MinionPrompt updateComponents(String promptId, List<PromptComponent> newComponents) {
    MinionPrompt prompt = getPromptOrThrow(promptId);
    
    prompt.getComponents().clear();
    for (PromptComponent component : newComponents) {
        prompt.getComponents().put(component.getType(), component);
    }
    
    return repository.save(prompt);
  }

  private MinionPrompt getPromptOrThrow(String promptId) {
    return repository.findById(promptId)
        .orElseThrow(() -> new PromptException.PromptNotFoundException(promptId));
  }
}
