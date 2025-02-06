package com.hls.minions.patient.tool;

import com.hls.minions.patient.entity.PatientInfo;
import com.hls.minions.patient.service.PatientService;
import com.hls.minions.patient.tool.PatientInfoRetrievalTool.Request;
import java.util.List;
import java.util.function.Function;

public class PatientInfoRetrievalTool implements Function<Request, PatientInfo> {

  private final PatientService patientService;

  public PatientInfoRetrievalTool(PatientService patientService) {
    this.patientService = patientService;
  }

  @Override public PatientInfo apply(Request request) {
    String phoneNumber = request.phone_number;
    String dob = request.patient_date_of_birth;
    String fullName = request.patient_full_name;
    return patientService.getRandomPatientWithUpdatedInfo(dob,phoneNumber,fullName).get();
  }

  public record Request(String phone_number, String patient_full_name, String patient_date_of_birth) {

  }

  public record Response(PatientInfo patientInfo) {}
}
