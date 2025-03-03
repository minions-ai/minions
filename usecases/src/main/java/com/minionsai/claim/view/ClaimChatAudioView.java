package com.minionsai.claim.view;

import com.minionsai.core.service.ResponseSupplier.Response;
import com.minionsai.core.view.ChatAudioView;
import com.vaadin.flow.router.Route;
import java.util.concurrent.CompletableFuture;

@Route("")
public class ClaimChatAudioView extends ChatAudioView {


  protected CompletableFuture<Response> getFuture(String requestId, String requestDetail) {
    return null;
  }

}