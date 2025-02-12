package com.hls.minions.patient.service;


import com.hls.minions.core.agent.BaseAgent;
import com.hls.minions.core.agent.BaseAudioAgent;
import com.hls.minions.core.service.AgentManager;
import com.hls.minions.patient.agent.AudioMasterAgent;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class PatientAudioAgentManager extends AgentManager {

  private final WebMToMp3Converter converter;
  public PatientAudioAgentManager(Builder chatClientBuilder, WebMToMp3Converter converter) {
    super(chatClientBuilder);
    this.converter = converter;
  }

  @Override protected BaseAgent createMasterAgent(String requestId, ChatMemory chatMemory) {
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
