package com.minionsai.claim.agent;

import com.minionsai.claude.agent.Minion;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.model.function.FunctionCallback;

@Slf4j
public class FraudInvestigationAgent extends Minion {

  public FraudInvestigationAgent(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
    super(chatClientBuilder, chatMemory);
    this.prompt = ResourceUtils.getText("classpath:agents/claim/fraud_investigation_agent.txt");
  }

  @Override protected String getPromptFilePath() {
    return "";
  }

  @Override protected FunctionCallback[] getAvailableTools() {
    return List.of("fraudCheckerTool", "historicalClaimsTool", "geoLocationVerifierTool", "customerCommunicationTool", "loggingTool").toArray(new String[0]);
  }

  @Override protected String getSystemPrompt() {
    return ResourceUtils.getText("classpath:fraud_investigation_agent.txt");
  }
}
