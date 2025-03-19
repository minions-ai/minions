package com.minionslab.core.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.common.exception.PromptException;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@ConditionalOnProperty(name = "minions.prompt.storage", havingValue = "file")
public class FilePromptRepository implements PromptRepository {

  private final String promptsDirectory;
  private final ObjectMapper objectMapper;

  public FilePromptRepository(
      @Value("${minions.prompt.directory}") String promptsDirectory,
      ObjectMapper objectMapper) {
    this.promptsDirectory = promptsDirectory;
    this.objectMapper = objectMapper;
    initializeDirectory();
  }

  private void initializeDirectory() {
    try {
      Files.createDirectories(Paths.get(promptsDirectory));
    } catch (IOException e) {
      throw new PromptException("Failed to create prompts directory", e);
    }
  }

  @Override
  public Optional<MinionPrompt> findById(String id) {
    // Assuming ID format: tenantId-type-name-version
    String[] parts = id.split("-");
    if (parts.length != 4) {
      throw new PromptException("Invalid prompt ID format: " + id);
    }

    return findByTypeAndNameAndVersionAndTenantId(
        MinionType.valueOf(parts[1]),
        parts[2],
        parts[3],
        parts[0]
    );
  }

  @Override
  public Optional<MinionPrompt> findByTypeAndNameAndVersionAndTenantId(
      MinionType type,
      String name,
      String version,
      String tenantId) {
    Path promptPath = buildPromptPath(tenantId, type, name, version);
    return readPromptFile(promptPath);
  }


  public List<MinionPrompt> findByTypeAndNameAndTenantIdOrderByVersionDesc(
      MinionType type,
      String name,
      String tenantId) {
    Path promptDir = buildPromptDirectoryPath(tenantId, type, name);
    return readPromptDirectory(promptDir);
  }

  @Override
  public List<MinionPrompt> findAllByTenantId(String tenantId) {
    Path tenantDir = Paths.get(promptsDirectory, tenantId);
    List<MinionPrompt> prompts = new ArrayList<>();

    try {
      Files.walk(tenantDir)
          .filter(path -> path.toString().endsWith(".json"))
          .forEach(path -> readPromptFile(path)
              .ifPresent(prompts::add));
    } catch (IOException e) {
      log.error("Error reading tenant directory: {}", tenantId, e);
    }

    return prompts;
  }

  @Override
  public MinionPrompt save(MinionPrompt prompt) {
    if (prompt.getId() == null) {
      prompt.setId(generateId(prompt));
    }

    Path promptPath = buildPromptPathFromPrompt(prompt);
    try {
      Files.createDirectories(promptPath.getParent());
      objectMapper.writeValue(promptPath.toFile(), prompt);
      return prompt;
    } catch (IOException e) {
      throw new PromptException("Failed to save prompt", e);
    }
  }

  @Override
  public void deleteById(String id) {
    findById(id).ifPresent(prompt -> {
      Path promptPath = buildPromptPathFromPrompt(prompt);
      try {
        Files.deleteIfExists(promptPath);
      } catch (IOException e) {
        throw new PromptException("Failed to delete prompt", e);
      }
    });
  }

  @Override public Optional<MinionPrompt> findByNameAndVersionAndTenantId(String promptName, String promptVersion, String tenantId) {
    return Optional.empty();
  }

  @Override
  public Optional<MinionPrompt> findLatestByTypeAndNameAndTenantId(
      MinionType type,
      String name,
      String tenantId) {
    return findByTypeAndNameAndTenantIdOrderByVersionDesc(type, name, tenantId)
        .stream()
        .findFirst();
  }

  @Override
  public List<MinionPrompt> findAllByTypeAndTenantId(
      MinionType type,
      String tenantId) {
    Path typeDir = Paths.get(promptsDirectory, tenantId, type.toString());
    List<MinionPrompt> prompts = new ArrayList<>();

    try {
      if (Files.exists(typeDir)) {
        Files.walk(typeDir)
            .filter(path -> path.toString().endsWith(".json"))
            .forEach(path -> readPromptFile(path)
                .ifPresent(prompts::add));
      }
    } catch (IOException e) {
      log.error("Error reading type directory: {}/{}", tenantId, type, e);
    }

    return prompts;
  }

  @Override
  public boolean existsByTypeAndNameAndVersionAndTenantId(
      MinionType type,
      String name,
      String version,
      String tenantId) {
    Path promptPath = buildPromptPath(tenantId, type, name, version);
    return Files.exists(promptPath);
  }

  private String generateId(MinionPrompt prompt) {
    return String.format("%s-%s-%s-%s",
        prompt.getTenantId(),
        prompt.getType(),
        prompt.getName(),
        prompt.getVersion());
  }

  private Path buildPromptPath(String tenantId, MinionType type, String name, String version) {
    return Paths.get(promptsDirectory, tenantId, type.toString(), name, version + ".json");
  }

  private Path buildPromptPathFromPrompt(MinionPrompt prompt) {
    return buildPromptPath(
        prompt.getTenantId(),
        prompt.getType(),
        prompt.getName(),
        prompt.getVersion()
    );
  }

  private Path buildPromptDirectoryPath(String tenantId, MinionType type, String name) {
    return Paths.get(promptsDirectory, tenantId, type.toString(), name);
  }

  private Optional<MinionPrompt> readPromptFile(Path path) {
    try {
      if (!Files.exists(path)) {
        return Optional.empty();
      }
      MinionPrompt prompt = objectMapper.readValue(path.toFile(), MinionPrompt.class);
      return Optional.of(prompt);
    } catch (IOException e) {
      log.error("Error reading prompt file: {}", path, e);
      return Optional.empty();
    }
  }

  private List<MinionPrompt> readPromptDirectory(Path dir) {
    try {
      if (!Files.exists(dir)) {
        return Collections.emptyList();
      }
      return Files.list(dir)
          .filter(path -> path.toString().endsWith(".json"))
          .map(this::readPromptFile)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .sorted(Comparator.comparing(MinionPrompt::getVersion).reversed())
          .collect(Collectors.toList());
    } catch (IOException e) {
      log.error("Error reading prompt directory: {}", dir, e);
      return Collections.emptyList();
    }
  }
} 