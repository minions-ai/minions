package com.minionsai.core.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

@Slf4j
public abstract class BaseResponseSupplier implements ResponseSupplier {

  private final Map<String, CompletableFuture<Response>> futureCache = new ConcurrentHashMap<>();
  private final AgentManager agentManager;

  public BaseResponseSupplier(AgentManager claimAgentManager) {
    this.agentManager = claimAgentManager;
  }

  /**
   * Starts processing a claim and stores the CompletableFuture.
   *
   * @return
   */
  @Async
  public CompletableFuture<Response> process(String requestId, String requestText) {
    if (requestId == null || requestId.isEmpty()) {
      requestId = agentManager.generateRequestId();
      log.info("Generated new Request ID: {}", requestId);
    } else {
      log.info("Processing existing Request ID: {}", requestId);
    }

    String finalRequestId = requestId;
    return CompletableFuture.supplyAsync(() -> {
      String response = agentManager.executePrompt(finalRequestId, requestText);
      return new Response(finalRequestId, response);
    });
  }


  public CompletableFuture<Response> getFuture(String requestId) {
    return futureCache.get(requestId);
  }

  protected void removeFuture(String requestId) {
    futureCache.remove(requestId);
  }

}
