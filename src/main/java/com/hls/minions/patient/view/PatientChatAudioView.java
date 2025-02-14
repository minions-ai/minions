package com.hls.minions.patient.view;

import com.hls.minions.core.service.ResponseSupplier.Response;
import com.hls.minions.core.view.ChatAudioView;
import com.hls.minions.patient.service.PatientService;
import com.vaadin.flow.router.Route;
import java.util.concurrent.CompletableFuture;

@Route("")
public class PatientChatAudioView extends ChatAudioView {

  private final PatientService patientService;

  public PatientChatAudioView(PatientService patientService) {
    this.patientService = patientService;
  }

  protected CompletableFuture<Response> getFuture(String requestId, String requestDetail) {
    return patientService.process(requestId, requestDetail);
  }

}
