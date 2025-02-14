package com.hls.minions.claim.agent;

import com.hls.minions.core.agent.MasterAgentPrompt;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClaimMasterAgentPrompt extends MasterAgentPrompt {

  public String systemPrompt() {
    Resource systemPrompt = new ClassPathResource("agents/claim/master_agent.txt");
    try {
      return Files.readString(systemPrompt.getFile().toPath(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error("Failed to load system prompt from master_agent.txt", e);
      throw new RuntimeException("Could not load system prompt", e); // Fail fast instead of returning ""
    }
  }
}
