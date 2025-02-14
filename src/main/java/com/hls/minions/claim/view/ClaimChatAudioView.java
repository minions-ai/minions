package com.hls.minions.claim.view;

import com.hls.minions.core.service.ResponseSupplier.Response;
import com.hls.minions.core.view.ChatAudioView;
import com.vaadin.flow.router.Route;
import java.util.concurrent.CompletableFuture;

@Route("")
public class ClaimChatAudioView extends ChatAudioView {


  protected CompletableFuture<Response> getFuture(String requestId, String requestDetail) {
    return null;
  }

}