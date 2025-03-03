package com.minionsai.core.service;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface ResponseSupplier {

  CompletableFuture<Response> process(String requestId, String requestText);

  /**
   * Retrieves a pending CompletableFuture.
   */



  record Response(String requestId, String response) {

  }
}
