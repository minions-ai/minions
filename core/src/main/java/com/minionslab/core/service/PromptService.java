package com.minionslab.core.service;

import com.minionslab.core.api.dto.CreatePromptRequest;
import com.minionslab.core.api.dto.PromptComponentRequest;
import com.minionslab.core.api.dto.PromptResponse;
import com.minionslab.core.api.dto.UpdatePromptRequest;
import com.minionslab.core.domain.MinionPrompt;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public interface PromptService {

  @Transactional MinionPrompt createPrompt(@Valid CreatePromptRequest request);

  PromptResponse getActiveVersion(String entityId, Instant pointInTime);

  PromptResponse getActiveVersion(String promptId);

  List<PromptResponse> getAllVersions(String entityId);

  Optional<MinionPrompt> getPromptByEntityIdAndVersion(String name, String version);

  Optional<MinionPrompt> getActivePromptAt(String entityId, Instant effectiveDate);

  Optional<MinionPrompt> getPrompt(String promptId);

  @Transactional PromptResponse updatePrompt(String promptId, UpdatePromptRequest request, boolean incrementVersionIfNeeded);

  MinionPrompt savePrompt(MinionPrompt samplePrompt);

  List<MinionPrompt> getPrompts();

  Optional<MinionPrompt> getPromptByEntityId(String entityId);

  void deletePrompt(String promptId);

  @Transactional PromptResponse updateComponent(String promptId, Instant updateEffectiveDate,
      PromptComponentRequest request,
      boolean incrementVersionIfNeeded);
}
