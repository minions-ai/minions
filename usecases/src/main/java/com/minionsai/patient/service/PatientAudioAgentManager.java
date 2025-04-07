package com.minionsai.patient.service;


import com.minionslab.core.domain.Minion;
import com.minionsai.core.agent.BaseAudioAgent;
import com.minionsai.core.service.AgentManager;
import com.minionsai.patient.agent.AudioMasterAgent;
import com.minionslab.core.service.ChatMemoryFactory;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class PatientAudioAgentManager extends AgentManager {

  private final WebMToMp3Converter converter;
  public PatientAudioAgentManager(Builder chatClientBuilder, WebMToMp3Converter converter, ChatMemoryFactory chatMemoryFactory) {
    super(chatClientBuilder, chatMemoryFactory);
    this.converter = converter;
  }

  @Override protected Minion createMasterAgent(String requestId, ChatMemory chatMemory) {
    return new AudioMasterAgent(chatClientBuilder, chatMemory);
  }

  @Override protected byte[] execute(String requestId, Object requestData, BaseAudioAgent masterAgent) {
    byte[] bytes= new byte[0];
    try {
      bytes = converter.convertWebMToMp3((byte[]) requestData);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return super.execute(requestId, bytes, masterAgent);
  }
}
