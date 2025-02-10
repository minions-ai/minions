package com.hls.minions.patient.service;

import com.hls.minions.core.service.AudioResponseSupplier;
import com.hls.minions.core.service.Response;
import com.hls.minions.core.view.Modality;
import com.hls.minions.patient.repository.PatientRepository;
import java.util.concurrent.CompletableFuture;

public class PatientAudioService extends PatientService implements AudioResponseSupplier {

  public PatientAudioService(PatientAgentManager patientAgentManager,
      PatientRepository patientRepository) {
    super(patientAgentManager, patientRepository);
  }

  @Override public CompletableFuture<Response> process(String requestId, String requestText, Modality modality) {
    return null;
  }
}
