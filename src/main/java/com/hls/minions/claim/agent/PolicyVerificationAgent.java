package com.hls.minions.claim.agent;


import com.hls.minions.core.agent.BaseAgent;
import com.hls.minions.core.annotation.AgentPrompt;
import com.hls.minions.core.service.prompt.AgentPromptLoader;
import com.hls.minions.core.service.prompt.ScopeType;
import com.hls.minions.core.service.prompt.SourceType;
import com.hls.minions.core.view.Modality;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;

@Slf4j
@AgentPrompt(scope = ScopeType.SYSTEM, source = SourceType.FILE, value = "agents/claim/policy_verification_agent.txt")
public class PolicyVerificationAgent extends BaseAgent {


  public PolicyVerificationAgent(AgentPromptLoader loader, Builder builder, ChatMemory chatMemory) {
    super(loader, builder, chatMemory);
  }

  @Override protected String[] getAvailableTools() {
    return List.of("policyLookupTool", "coverageCheckerTool", "premiumValidatorTool", "customerCommunicationTool", "loggingTool")
        .toArray(new String[0]);
  }

  @Override protected String getSystemPrompt() {
    return ResourceUtils.getText("classpath:policy_verification_agent.txt");
  }
}
