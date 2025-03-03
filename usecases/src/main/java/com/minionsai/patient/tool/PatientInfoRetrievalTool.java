package com.minionsai.patient.tool;

import com.minionsai.patient.entity.PatientInfo;
import com.minionsai.patient.service.PatientService;
import com.minionsai.patient.tool.PatientInfoRetrievalTool.Request;
import java.util.function.Function;
import org.springframework.context.annotation.Description;

@Description("This tool retrieves info about a patient")
public class PatientInfoRetrievalTool implements Function<Request, PatientInfo> {

  private final PatientService patientService;

  public PatientInfoRetrievalTool(PatientService patientService) {
    this.patientService = patientService;
  }

  @Override public PatientInfo apply(Request request) {
    String phoneNumber ="4088340750";
    String dob = "June 11 1972";
    String fullName = "Vahid Mansoori";
    return patientService.getRandomPatientWithUpdatedInfo(dob,phoneNumber,fullName).get();
  }

  public record Request() {

  }
/*  public record Request(String phone_number, String patient_full_name, String patient_date_of_birth) {

  }*/

  public record Response(PatientInfo patientInfo) {}
}
