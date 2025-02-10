package com.hls.minions.patient.agent;

import com.hls.minions.core.agent.BaseAgent;
import com.hls.minions.core.annotation.AgentPrompt;
import com.hls.minions.core.service.prompt.ScopeType;
import com.hls.minions.core.service.prompt.SourceType;
import com.hls.minions.core.view.Modality;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;

@AgentPrompt(scope = ScopeType.SYSTEM, source = SourceType.FILE, value = "agents/patient/master_agent.txt")
public class MasterAgent extends BaseAgent {

  public MasterAgent(Builder chatClientBuilder,
      ChatMemory chatMemory, Modality modality) {
    super(loader, chatClientBuilder, chatMemory, modality);
  }


  @Override protected String[] getAvailableTools() {
    return List.of("patientInformationTool", "patientReportGeneratorTool").toArray(new String[0]);
  }
}
