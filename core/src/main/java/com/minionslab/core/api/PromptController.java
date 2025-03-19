package com.minionslab.core.api;

import com.minionslab.core.api.dto.CreatePromptRequest;
import com.minionslab.core.api.dto.PromptResponse;
import com.minionslab.core.api.dto.UpdatePromptRequest;
import com.minionslab.core.common.exception.PromptException;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/prompts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Prompt Management", description = "APIs for managing minion prompts")
public class PromptController {

    private final PromptService promptService;

    @PostMapping
    @Operation(summary = "Create a new prompt")
    @ApiResponse(responseCode = "201", description = "Prompt created successfully")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromptResponse> createPrompt(
            @Valid @RequestBody CreatePromptRequest request) {
        log.debug("Creating new prompt: {}", request.getName());
        
        MinionPrompt prompt = request.toMinionPrompt();
        MinionPrompt savedPrompt = promptService.savePrompt(prompt);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PromptResponse.fromMinionPrompt(savedPrompt));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a prompt by ID")
    public ResponseEntity<PromptResponse> getPrompt(
            @Parameter(description = "Prompt ID") @PathVariable String id) {
        return promptService.getPromptById(id)
                .map(prompt -> ResponseEntity.ok(PromptResponse.fromMinionPrompt(prompt)))
                .orElseThrow(() -> new PromptException.PromptNotFoundException("Prompt not found: " + id));
    }

    @GetMapping
    @Operation(summary = "Get all prompts for a tenant")
    public ResponseEntity<List<PromptResponse>> getAllPrompts(
            @Parameter(description = "Tenant ID") 
            @RequestParam String tenantId) {
        List<PromptResponse> prompts = promptService.getAllPrompts(tenantId)
                .stream()
                .map(PromptResponse::fromMinionPrompt)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get all prompts of a specific type")
    public ResponseEntity<List<PromptResponse>> getPromptsByType(
            @Parameter(description = "Prompt type") @PathVariable MinionType type,
            @Parameter(description = "Tenant ID") @RequestParam String tenantId) {
        List<PromptResponse> prompts = promptService.getPromptsByType(type, tenantId)
                .stream()
                .map(PromptResponse::fromMinionPrompt)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(prompts);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing prompt")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromptResponse> updatePrompt(
            @Parameter(description = "Prompt ID") @PathVariable String id,
            @Valid @RequestBody UpdatePromptRequest request) {
        MinionPrompt existingPrompt = promptService.getPromptById(id)
                .orElseThrow(() -> new PromptException.PromptNotFoundException("Prompt not found: " + id));
        
        MinionPrompt updatedPrompt = request.updateMinionPrompt(existingPrompt);
        MinionPrompt savedPrompt = promptService.savePrompt(updatedPrompt);
        
        return ResponseEntity.ok(PromptResponse.fromMinionPrompt(savedPrompt));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a prompt")
    @ApiResponse(responseCode = "204", description = "Prompt deleted successfully")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePrompt(
            @Parameter(description = "Prompt ID") @PathVariable String id) {
        promptService.deletePrompt(id);
        return ResponseEntity.noContent().build();
    }
} 