package com.hls.minions.patient.tool;

import com.hls.minions.patient.service.PatientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import com.hls.minions.patient.report.PatientReportGeneratorTool;

@Component
public class PatientToolConfiguration {

  @Bean
  @Description("Retrieves patient's information from the patient repository")
  public PatientInfoRetrievalTool patientInformationTool(PatientService patientService) {
    return new PatientInfoRetrievalTool(patientService);
  }

  @Bean
  @Description("Generates a PDF report of the patient assessment")
  public PatientReportGeneratorTool patientReportGeneratorTool() {
    return new PatientReportGeneratorTool();
  }


}
