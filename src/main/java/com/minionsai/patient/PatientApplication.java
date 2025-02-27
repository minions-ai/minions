package com.minionsai.patient;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Push
@SpringBootApplication(scanBasePackages = {"com.hls.minions.core","com.hls.minions.patient"})
@EnableMongoRepositories(basePackages = "com.hls.minions.patient.repository") // Specify the package of your repositories
public class PatientApplication implements AppShellConfigurator {

  public static void main(String[] args) {
    SpringApplication.run(PatientApplication.class, args);
  }

}
