package com.minionsai.claim.view;

import com.minionsai.claim.service.ClaimService;
import com.minionsai.core.view.ChatView;
import com.minionsai.core.service.ResponseSupplier;
import com.vaadin.flow.router.Route;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;

//@Push  // Enables push support for asynchronous UI updates
@Route("/claims")
public class ClaimChatView extends ChatView {

  @Autowired
  private ClaimService claimService;

  @Override protected CompletableFuture<ResponseSupplier.Response> getFuture(String requestId, String requestDetail) {
    return claimService.process(requestId, requestDetail);
  }
}
