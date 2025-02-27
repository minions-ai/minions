package com.minionsai.patient.view;

import com.minionsai.core.service.ResponseSupplier.Response;
import com.minionsai.core.view.ChatAudioView;
import com.minionsai.patient.service.PatientService;
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
