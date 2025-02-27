package com.minionsai.patient.view;

import com.minionsai.core.view.ChatView;
import com.minionsai.patient.service.PatientService;
import com.minionsai.core.service.ResponseSupplier.Response;
import com.vaadin.flow.router.Route;
import java.util.concurrent.CompletableFuture;

@Route("/patient")
public class PatientChatView extends ChatView {

  private final PatientService patientService;

  public PatientChatView(PatientService patientService) {
    this.patientService = patientService;
  }

  @Override protected CompletableFuture<Response> getFuture(String requestId, String requestDetail) {
    return patientService.process(requestId, requestDetail);
  }
}
