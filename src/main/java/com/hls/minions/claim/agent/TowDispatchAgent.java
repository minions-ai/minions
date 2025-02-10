package com.hls.minions.claim.agent;

import com.hls.minions.core.agent.BaseAgent;
import com.hls.minions.core.annotation.AgentPrompt;
import com.hls.minions.core.service.prompt.AgentPromptLoader;
import com.hls.minions.core.service.prompt.ScopeType;
import com.hls.minions.core.service.prompt.SourceType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;

@Slf4j
@AgentPrompt(scope = ScopeType.SYSTEM, source = SourceType.FILE, value = "agents/claim/tow_dispatch_agent.txt")
public class TowDispatchAgent extends BaseAgent {

  public TowDispatchAgent(AgentPromptLoader loader, Builder chatClientBuilder, ChatMemory chatMemory) {
    super(loader, chatClientBuilder, chatMemory);
  }

  @Override protected String[] getAvailableTools() {
    return List.of("locationServiceTool", "towDispatchTool", "etaEstimatorTool", "smsNotifierTool", "customerCommunicationTool",
        "loggingTool").toArray(new String[0]);
  }

}
