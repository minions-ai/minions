package com.hls.minions.core.service;

import com.hls.minions.core.view.Modality;
import java.util.concurrent.CompletableFuture;

public interface AudioResponseSupplier extends ResponseSupplier {

  CompletableFuture<Response> process(String requestId, String requestText, Modality modality);

}
