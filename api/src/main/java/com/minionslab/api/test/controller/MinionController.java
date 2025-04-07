package com.minionslab.api.test.controller;

import com.minionslab.api.test.controller.dto.CreateMinionRequest;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.service.MinionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing minions.
 */
@RestController
@RequestMapping("/v1/minions")
@RequiredArgsConstructor
@Tag(name = "Minion Management", description = "APIs for managing minions")
@Validated
public class MinionController {

  private final MinionService minionService;

  @PostMapping
  @Operation(summary = "Create a new minion")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Minion created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Minion> createMinion(@RequestBody @Valid CreateMinionRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(minionService.createMinion(request));
  }

  @PostMapping("/{id}/process")
  @Operation(summary = "Process a request using a minion")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request processed successfully"),
      @ApiResponse(responseCode = "404", description = "Minion not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<String> processRequest(
      @Parameter(description = "ID of the minion to use") @PathVariable String id,
      @Parameter(description = "Request to process") @RequestBody String request) {
    return ResponseEntity.ok(minionService.processRequest(id, request));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a minion by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Minion found"),
      @ApiResponse(responseCode = "404", description = "Minion not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Minion> getMinion(
      @Parameter(description = "ID of the minion to retrieve") @PathVariable String id) {
    return ResponseEntity.ok(minionService.getMinion(id));
  }
} 