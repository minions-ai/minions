package com.hls.minions.claim.agent;


import com.hls.minions.core.agent.BaseAgent;
import com.hls.minions.core.annotation.AgentPrompt;
import com.hls.minions.core.view.Modality;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;

@Slf4j
@AgentPrompt("agents/claim/policy_verification_agent.txt")
public class PolicyVerificationAgent extends BaseAgent {


  public PolicyVerificationAgent(Builder builder, ChatMemory chatMemory) {
    super(builder, chatMemory);
  }

  public PolicyVerificationAgent(Builder chatClientBuilder, ChatMemory chatMemory, Modality modality) {
    super(chatClientBuilder, chatMemory, modality);
  }

  @Override protected String[] getAvailableTools() {
    return List.of("policyLookupTool", "coverageCheckerTool", "premiumValidatorTool", "customerCommunicationTool", "loggingTool")
        .toArray(new String[0]);
  }

  @Override protected String getSystemPrompt() {
    return ResourceUtils.getText("classpath:policy_verification_agent.txt");
  }
}
