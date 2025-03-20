package com.minionslab.core.api;

import com.minionslab.core.api.dto.CreateMinionRequest;
import com.minionslab.core.api.dto.MinionRequest;
import com.minionslab.core.api.dto.MinionResponse;
import com.minionslab.core.common.exception.MinionException.CreationException;
import com.minionslab.core.service.RequestHandlerService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/minions")
public class MinionController {

  private final RequestHandlerService requestHandler;

  public MinionController(RequestHandlerService requestHandler) {
    this.requestHandler = requestHandler;
  }

  /**
   * Create a new minion
   *
   * @param request The minion creation request
   * @return The created minion metadata
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<MinionResponse> createMinion(
      @RequestBody @Validated CreateMinionRequest request) {
    log.info("Creating minion with request: {}", request);
    ResponseEntity<MinionResponse> body = null;
    try {
      body = ResponseEntity.status(HttpStatus.CREATED)
          .body(requestHandler.createMinion(request));
    } catch (CreationException e) {
      body = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    return body;
  }

  /**
   * Process a prompt using a specific minion
   *
   * @param minionId The ID of the minion to use
   * @param request  The prompt processing request
   * @return CompletableFuture containing the response
   */
  @PostMapping("/{minionId}/process")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public CompletableFuture<ResponseEntity<MinionResponse>> processPrompt(
      @PathVariable String minionId,
      @RequestBody @Validated MinionRequest request) {
    log.info("Processing prompt with minion {}: {}", minionId, request);
    return requestHandler.handleRequest(minionId, request)
        .thenApply(ResponseEntity::ok)
        .exceptionally(throwable -> {
          log.error("Error processing request", throwable);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        });
  }

  /**
   * Get minion details by ID
   *
   * @param minionId The ID of the minion
   * @return The minion details
   */
  @GetMapping("/{minionId}")
  public ResponseEntity<MinionResponse> getMinionById(@PathVariable String minionId) {
    return ResponseEntity.ok(requestHandler.getMinionById(minionId));
  }

  /**
   * Get health status
   *
   * @return Health status information
   */
  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> healthCheck() {
    Map<String, String> healthStatus = new HashMap<>();
    healthStatus.put("status", "UP");
    healthStatus.put("message", "API is running smoothly");
    return ResponseEntity.ok(healthStatus);
  }

  /**
   * Handle exceptions
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
    Map<String, Object> error = new HashMap<>();
    error.put("error", ex.getMessage());
    error.put("timestamp", Optional.of(System.currentTimeMillis()));

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    if (ex instanceof IllegalArgumentException) {
      status = HttpStatus.BAD_REQUEST;
    }

    return ResponseEntity.status(status).body(error);
  }
} 