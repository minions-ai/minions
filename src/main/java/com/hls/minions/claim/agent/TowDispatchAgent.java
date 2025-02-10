package com.hls.minions.claim.agent;

import com.hls.minions.core.agent.BaseAgent;
import com.hls.minions.core.annotation.AgentPrompt;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

@Slf4j
@AgentPrompt("agents/claim/tow_dispatch_agent.txt")
public class TowDispatchAgent extends BaseAgent {

  public TowDispatchAgent(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
    super(chatClientBuilder, chatMemory);
  }

  @Override protected String[] getAvailableTools() {
    return List.of("locationServiceTool", "towDispatchTool", "etaEstimatorTool", "smsNotifierTool", "customerCommunicationTool",
        "loggingTool").toArray(new String[0]);
  }

}
