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
@AgentPrompt(scope = ScopeType.SYSTEM, source = SourceType.FILE, value ="agents/claim/claim_intake_agent.txt")
public class ClaimIntakeAgent extends BaseAgent {


  public ClaimIntakeAgent(Builder builder, ChatMemory chatMemory) {
    super(builder, chatMemory);
  }

  public ClaimIntakeAgent(Builder chatClientBuilder, ChatMemory chatMemory, Modality modality) {
    super(loader, chatClientBuilder, chatMemory, modality);
  }


  @Override protected String[] getAvailableTools() {
    return List.of("customerCommunicationTool", "loggingTool").toArray(new String[0]);
  }

  @Override protected String getSystemPrompt() {
    return ResourceUtils.getText("classpath:claim_intake_agent.txt");
  }
}

