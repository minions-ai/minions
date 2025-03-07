package com.hls.minions.patient.service;

import com.hls.minions.core.service.BaseResponseSupplier;
import com.hls.minions.patient.entity.PatientInfo;
import com.hls.minions.patient.repository.PatientRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PatientService extends BaseResponseSupplier {

  private final PatientRepository repository;

  public PatientService(PatientAgentManager patientAgentManager,PatientRepository patientRepository) {
    super(patientAgentManager);
    this.repository = patientRepository;
  }

  public Optional<PatientInfo> getRandomPatientWithUpdatedInfo(String newDob, String newPhone, String fullName) {
    Optional<PatientInfo> randomPatient = repository.findRandomPatient();

    return randomPatient.map(patient -> {
      PatientInfo updatedPatient = new PatientInfo(
          patient.id(),
          fullName,
          newDob, // Update DOB
          patient.gender(),
          patient.currentLocation(),
          patient.emergencyContactName(),
          newPhone, // Update Phone
          patient.knownMedicalConditions(),
          patient.allergies(),
          patient.currentMedications(),
          patient.pastMedications(),
          patient.mentalHealthHistory(),
          patient.pastHospitalizations(),
          patient.primaryCarePhysician(),
          patient.psychiatrist(),
          patient.previousEmergencyVisits()
      );
      return repository.save(updatedPatient);
    });
  }
}
