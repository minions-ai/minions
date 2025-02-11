package com.hls.minions.claim.view;

import com.hls.minions.claim.service.ClaimService;
import com.hls.minions.core.view.ChatView;
import com.hls.minions.core.service.ResponseSupplier;
import com.vaadin.flow.router.Route;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;

//@Push  // Enables push support for asynchronous UI updates
@Route("/claim")
public class ClaimChatView extends ChatView {

  @Autowired
  private ClaimService claimService;

  @Override protected CompletableFuture<ResponseSupplier.Response> getFuture(String requestId, String requestDetail) {
    return claimService.process(requestId, requestDetail);
  }
}
