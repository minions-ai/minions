package com.hls.minions.patient.view;

import com.hls.minions.core.service.Response;
import com.hls.minions.core.view.AudioToggleChatView;
import com.hls.minions.core.view.Modality;
import com.hls.minions.patient.service.PatientService;
import com.vaadin.flow.router.Route;
import java.util.concurrent.CompletableFuture;

@Route("/patient/audio")
public class PatientAudioChatView extends AudioToggleChatView {

  private final PatientService patientService;

  public PatientAudioChatView(PatientService patientService) {
    this.patientService = patientService;
  }

  @Override protected CompletableFuture<Response> getFuture(String requestId, String requestDetail) {
    return patientService.process(requestId, requestDetail, Modality.AUDIO);
  }
}
