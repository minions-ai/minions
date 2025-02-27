package com.minionsai.claim.agent;

import com.minionsai.core.agent.BaseAgent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

@Slf4j
public class MasterAgent extends BaseAgent {


  public MasterAgent(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
    super(chatClientBuilder, chatMemory);

  }

  protected String getPromptFilePath() {
    return "agents/claim/master_agent.txt";
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
