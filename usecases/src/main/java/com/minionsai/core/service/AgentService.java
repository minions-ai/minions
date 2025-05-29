package com.minionsai.core.service;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.time.Duration;
import java.util.stream.Stream;
import reactor.core.publisher.Flux;

@SpringComponent
@UIScope public class AgentService {

  public Flux<String> processRequest(String agentName, String prompt) {
    return Flux.fromStream(Stream.of(
            agentName + " is processing request...",
            agentName + " is analyzing: " + prompt,
            agentName + " is finalizing response...",
            agentName + " results: " + prompt
        ))
        .delayElements(Duration.ofSeconds(1));
  }
}