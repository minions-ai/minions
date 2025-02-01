package com.hls.minions.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterAgent {

  private final String prompt;
  @Autowired
  private ChatClient.Builder chatClientBuilder;


  public MasterAgent() {
    prompt = ResourceUtils.getText("classpath:master_agent.txt");
  }


  public String processPrompt(String userRequest) {
    ChatClient chatClient = chatClientBuilder
        .defaultFunctions("policyVerificationTool"
            , "coverageVerificationAgent"
            , "damageAssessmentAgent"
            , "fraudDetectionAgent", "claimSubmissionAgent")
        .defaultAdvisors(new PromptChatMemoryAdvisor(new InMemoryChatMemory()))
        .build();

    String response = chatClient.prompt().system(prompt).user(userRequest).call().content();

    log.info("Response from the LLM {}", response);
    return response;
  }
}
