package com.hls.minions.claim.agent;

import com.hls.minions.core.agent.BaseAgent;
import com.hls.minions.core.annotation.AgentPrompt;
import com.hls.minions.core.service.prompt.ScopeType;
import com.hls.minions.core.service.prompt.SourceType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

@Slf4j
@AgentPrompt(scope = ScopeType.SYSTEM, source = SourceType.FILE, value ="agents/claim/master_agent.txt")
public class MasterAgent extends BaseAgent {


  public MasterAgent(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
    super(chatClientBuilder, chatMemory);

  }


  @Override protected String[] getAvailableTools() {
    return List.of(
        "adjusterAssignerTool",
        "claimSubmissionTool",
        "coverageCheckerTool",
        "documentGeneratorTool",
        "fraudCheckerTool",
        "geoLocationTool",
        "historicalClaimsTool",
        "policyDatabaseTool",
        "premiumValidatorTool",
        "towDispatchTool"
    ).toArray(new String[0]);
  }

  @Override protected String getSystemPrompt() {
    return this.prompt;
  }


}
