package com.minionsai.patient.view;

import com.minionsai.core.service.ResponseSupplier.Response;
import com.minionsai.core.view.AudioWebSocketView;
import com.minionsai.patient.service.PatientService;
import com.vaadin.flow.router.Route;
import java.util.concurrent.CompletableFuture;

@Route("audio")
public class PatientAudioView extends AudioWebSocketView {

  private final PatientService patientService;

  public PatientAudioView(PatientService patientService) {
    this.patientService = patientService;
  }

protected CompletableFuture<Response> getFuture(String requestId, String requestDetail) {
    return patientService.process(requestId, requestDetail);
  }
}
