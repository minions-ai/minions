package com.minionsai.patient.agent;

import com.minionslab.core.domain.Minion;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.model.function.FunctionCallback;

public class MasterAgent extends Minion {

  public MasterAgent(Builder chatClientBuilder,
      ChatMemory chatMemory) {
    super(chatClientBuilder, chatMemory);
  }

  @Override protected String getPromptFilePath() {
    return "agents/patient/master_agent.txt";
  }

  @Override protected FunctionCallback[] getAvailableTools() {
    return List.of("patientInformationTool","patientReportGeneratorTool").toArray(new String[0]);
  }
}
