package com.minionsai.patient.tool;

import com.minionsai.patient.service.PatientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;


//@Component
public class PatientToolConfiguration {

  @Bean
  @Description("Retrieves patient's information from the patient repository")
  public PatientInfoRetrievalTool patientInfoRetrievalTool(PatientService patientService) {
    return new PatientInfoRetrievalTool(patientService);
  }

  @Bean
  @Description("Generates a PDF report of the patient assessment")
  public PatientReportGeneratorTool patientReportGeneratorTool() {
    return new PatientReportGeneratorTool();
  }


}
