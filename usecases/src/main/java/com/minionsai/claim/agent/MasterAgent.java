package com.minionsai.claim.agent;

import com.minionslab.core.domain.Minion;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.model.function.FunctionCallback;

@Slf4j
public class MasterAgent extends Minion {


  public MasterAgent(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
    super(chatClientBuilder, chatMemory);

  }

  protected String getPromptFilePath() {
    return "agents/claim/master_agent.txt";
  }


  @Override protected FunctionCallback[] getAvailableTools() {
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
