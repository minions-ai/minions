package com.hls.minions.claim.controller;

import com.hls.minions.claim.dto.ClaimRequest;
import com.hls.minions.claim.service.ClaimService;

import com.hls.minions.core.service.ResponseSupplier.Response;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/claims")
public class ClaimController {

  private final ClaimService claimService;

  public ClaimController(ClaimService claimService) {
    this.claimService = claimService;
  }

  private static Response getErrorResponse(String errorText) {
    return new Response(null, errorText);
  }

  @PostMapping("/process")
  public CompletableFuture<ResponseEntity<String>> startClaim(@RequestBody ClaimRequest request) {
    return claimService.process(request.getRequestId(), request.getRequestText())
        .thenApply(response -> ResponseEntity.ok("requestId=" + response.requestId() + ", response: " + response.response()))
        .exceptionally(ex -> {
          log.error("Error processing claim request", ex);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing claim.");
        });
  }

  @GetMapping("/status/{requestId}")
  public ResponseEntity<ClaimService.Response> checkClaimStatus(@PathVariable String requestId) {
    CompletableFuture<Response> future = claimService.getFuture(requestId);

    if (future == null) {
      return ResponseEntity.status(404).body(getErrorResponse("Request ID not found"));
    }

    if (future.isDone()) {
      try {
        return ResponseEntity.ok(future.get()); // Return completed result
      } catch (Exception e) {
        return ResponseEntity.status(500).body(getErrorResponse("Error processing request"));
      }
    } else {
      return ResponseEntity.status(202).body(getErrorResponse("Processing still in progress..."));
    }
  }
}
