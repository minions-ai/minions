package com.hls.minions.patient.view;

import com.hls.minions.core.service.ResponseSupplier.Response;
import com.hls.minions.core.view.AudioWebSocketView;
import com.hls.minions.patient.service.PatientService;
import com.vaadin.flow.router.Route;
import java.util.concurrent.CompletableFuture;

@Route("")
public class PatientAudioView extends AudioWebSocketView {

  private final PatientService patientService;

  public PatientAudioView(PatientService patientService) {
    this.patientService = patientService;
  }

  @Override protected CompletableFuture<Response> getFuture(String requestId, String requestDetail) {
    return patientService.process(requestId, requestDetail);
  }
}
