package com.hls.minions.claim.agent;

import com.hls.minions.core.agent.BaseAgent;
import com.hls.minions.core.annotation.AgentPrompt;
import com.hls.minions.core.service.prompt.ScopeType;
import com.hls.minions.core.service.prompt.SourceType;
import com.hls.minions.core.view.Modality;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;

@Slf4j
@AgentPrompt(scope = ScopeType.SYSTEM, source = SourceType.FILE, value ="agents/claim/fraud_investigation_agent.txt")
public class FraudInvestigationAgent extends BaseAgent {

  public FraudInvestigationAgent(Builder chatClientBuilder, ChatMemory chatMemory, Modality modality) {
    super(loader, chatClientBuilder, chatMemory, modality);
  }



  @Override protected String[] getAvailableTools() {
    return List.of("fraudCheckerTool", "historicalClaimsTool", "geoLocationVerifierTool", "customerCommunicationTool", "loggingTool").toArray(new String[0]);
  }

  @Override protected String getSystemPrompt() {
    return ResourceUtils.getText("classpath:fraud_investigation_agent.txt");
  }
}
