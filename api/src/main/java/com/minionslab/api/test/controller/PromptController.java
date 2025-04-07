package com.minionslab.api.test.controller;

import com.minionslab.api.test.controller.dto.CreatePromptRequest;
import com.minionslab.api.test.controller.dto.PromptComponentRequest;
import com.minionslab.api.test.controller.dto.PromptResponse;
import com.minionslab.api.test.controller.dto.UpdatePromptRequest;
import com.minionslab.core.common.exception.PromptException.PromptNotFoundException;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/prompts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Prompt Management", description = "APIs for managing minion prompts")
public class PromptController {

  private final PromptService promptService;

  @PostMapping
  @Operation(summary = "Create a new prompt", description = "Creates a new prompt with optional components. The prompt will be created with version 1.0.0 by default.")
  @ApiResponse(responseCode = "201", description = "Prompt created successfully")
//  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PromptResponse> createPrompt(
      @Valid @RequestBody CreatePromptRequest request) {

    Map<PromptType, PromptComponent> components = request.getComponents().stream()
        .collect(Collectors.toMap(
            PromptComponentRequest::getType,
            PromptComponentRequest::toPromptComponent
        ));

    MinionPrompt prompt = promptService.createPrompt(
        request.getEntityId(),
        request.getDescription(),
        request.getVersion(),
        components,
        request.getMetadata(),
        request.getEffectiveDate(),
        request.getExpiryDate()
    );

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(PromptResponse.fromMinionPrompt(prompt));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a prompt by ID", description = "Retrieves the latest version of a prompt by its ID.")
  public ResponseEntity<PromptResponse> getPrompt(
      @Parameter(description = "Prompt ID") @PathVariable String id) {
    return ResponseEntity.ok(promptService.getPrompt(id).map(PromptResponse::fromMinionPrompt)
        .orElseThrow(() -> new PromptNotFoundException("Prompt not found")));
  }

  @GetMapping("/{entityId}/{version}")
  @Operation(summary = "Get a specific version of a prompt")
  public ResponseEntity<PromptResponse> getPromptByEntityIdAndVersion(@PathVariable String entityId, @PathVariable String version) {
    return ResponseEntity.ok(promptService.getPromptByEntityIdAndVersion(entityId, version).map(PromptResponse::fromMinionPrompt).get());
  }


  @PutMapping("/{id}")
  @Operation(
      summary = "Update an existing prompt",
      description = "Updates a prompt's metadata and components. If the prompt is deployed (in production), " +
          "setting incrementVersionIfNeeded=true will create a new version. Updates to expired prompts are not allowed."
  )
  @ApiResponse(responseCode = "200", description = "Prompt updated successfully")
  @ApiResponse(responseCode = "400", description = "Cannot update expired prompt")
  @ApiResponse(responseCode = "409", description = "Cannot update deployed prompt without version increment")
//  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PromptResponse> updatePrompt(
      @Parameter(description = "Prompt ID") @PathVariable String id,
      @Valid @RequestBody UpdatePromptRequest request,
      @Parameter(description = "If true, creates a new version when updating a deployed prompt")
      @RequestParam(defaultValue = "false") boolean incrementVersionIfNeeded) {

    Map<PromptType, PromptComponent> components = request.getComponents().stream()
        .collect(Collectors.toMap(
            PromptComponentRequest::getType,
            PromptComponentRequest::toPromptComponent
        ));

    MinionPrompt updatedPrompt = promptService.updatePrompt(
        id,
        request.getDescription(),
        components,
        request.getMetadata(),
        request.getEffectiveDate(),
        request.getExpiryDate(),
        incrementVersionIfNeeded
    );

    return ResponseEntity.ok(PromptResponse.fromMinionPrompt(updatedPrompt));
  }

  @PutMapping("/{id}/components/{componentType}")
  @Operation(
      summary = "Update a specific component",
      description = "Updates a specific component of a prompt. If the prompt is deployed (in production), " +
          "setting incrementVersionIfNeeded=true will create a new version. Updates to expired prompts are not allowed."
  )
  @ApiResponse(responseCode = "200", description = "Component updated successfully")
  @ApiResponse(responseCode = "400", description = "Cannot update expired prompt")
  @ApiResponse(responseCode = "404", description = "Prompt or component not found")
  @ApiResponse(responseCode = "409", description = "Cannot update deployed prompt without version increment")
  public ResponseEntity<PromptResponse> updateComponent(
      @Parameter(description = "Prompt ID") @PathVariable String id,
      @Parameter(description = "Type of component to update") @PathVariable PromptType componentType,
      @Valid @RequestBody PromptComponentRequest request,
      @Parameter(description = "If true, creates a new version when updating a deployed prompt")
      @RequestParam(defaultValue = "false") boolean incrementVersionIfNeeded) {
    log.debug("Updating component {} for prompt: {}", componentType, id);

    MinionPrompt updatedPrompt = promptService.updateComponent(
        id,
        Instant.now(),
        componentType,
        request.getContent(),
        request.getMetadatas(),
        incrementVersionIfNeeded
    );

    return ResponseEntity.ok(PromptResponse.fromMinionPrompt(updatedPrompt));
  }

  @DeleteMapping("/{promptId}")
  @Operation(summary = "Delete an existing prompt")
//  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deletePrompt(@PathVariable String promptId) {
    promptService.deletePrompt(promptId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(summary = "Get all prompts", description = "Returns all prompts for the current tenant")
//@PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<PromptResponse>> getPrompts() {
    return ResponseEntity.ok(promptService
        .getPrompts().stream().map(PromptResponse::fromMinionPrompt).toList());
  }
} 