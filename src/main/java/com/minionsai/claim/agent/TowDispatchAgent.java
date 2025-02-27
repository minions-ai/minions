package com.minionsai.claim.agent;

import com.minionsai.core.agent.BaseAgent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

@Slf4j
public class TowDispatchAgent extends BaseAgent {

  public TowDispatchAgent(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
    super(chatClientBuilder, chatMemory);
    this.prompt = ResourceUtils.getText("classpath:agents/claim/tow_dispatch_agent.txt");
  }

  @Override protected String getPromptFilePath() {
    return "";
  }

  @Override protected String[] getAvailableTools() {
    return List.of("locationServiceTool", "towDispatchTool", "etaEstimatorTool", "smsNotifierTool", "customerCommunicationTool", "loggingTool").toArray(new String[0]);
  }

}
