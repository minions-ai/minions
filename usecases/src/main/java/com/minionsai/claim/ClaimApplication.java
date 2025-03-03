package com.minionsai.claim;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Push
@SpringBootApplication(scanBasePackages = {"com.hls.minions.claim", "com.hls.minions.core"})

public class ClaimApplication implements AppShellConfigurator {

  public static void main(String[] args) {
    SpringApplication.run(ClaimApplication.class, args);
  }

}
