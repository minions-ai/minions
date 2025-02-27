package com.minionsai.claim.service;


import com.minionsai.core.service.BaseResponseSupplier;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClaimService extends BaseResponseSupplier {

  // Cache for pending claims


  public ClaimService(ClaimAgentManager claimAgentManager) {
    super(claimAgentManager);
  }

  /**
   * Allows external agents to retrieve and complete the CompletableFuture.
   */
  public void completeClaim(String requestId, String responseText) {
    CompletableFuture<ClaimService.Response> future = getFuture(requestId);
    if (future != null) {
      future.complete(new Response(requestId, responseText));
      log.info("Completed future for Request ID: {}", requestId);
      removeFuture(requestId); // Clean up after completion
    } else {
      log.warn("Request ID {} not found or already completed.", requestId);
    }
  }


}
