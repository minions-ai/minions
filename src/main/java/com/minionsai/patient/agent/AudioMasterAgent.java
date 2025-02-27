package com.minionsai.patient.agent;

import com.minionsai.core.agent.BaseAudioAgent;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

public class AudioMasterAgent extends BaseAudioAgent {

  public AudioMasterAgent(ChatClient.Builder chatClientBuilder,
      ChatMemory chatMemory) {
    super(chatClientBuilder, chatMemory);
  }

  @Override protected String getPromptFilePath() {
    return "agents/patient/master_agent.txt";
  }

  @Override protected String[] getAvailableTools() {
    return List.of("patientInformationTool","patientReportGeneratorTool").toArray(new String[0]);
  }


}
