package com.hls.minions;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@Push
@EnableMongoRepositories(basePackages = "com.hls.minions.patient.repository") // Specify the package of your repositories

public class MinionsAiApplication implements AppShellConfigurator {

  public static void main(String[] args) {
    SpringApplication.run(MinionsAiApplication.class, args);
  }

}
