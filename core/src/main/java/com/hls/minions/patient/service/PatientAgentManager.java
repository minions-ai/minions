package com.hls.minions.patient.service;


import com.hls.minions.core.agent.BaseAgent;
import com.hls.minions.core.service.AgentManager;
import com.hls.minions.patient.agent.MasterAgent;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class PatientAgentManager extends AgentManager {

  public PatientAgentManager(Builder chatClientBuilder) {
    super(chatClientBuilder);
  }

  @Override protected BaseAgent createMasterAgent(String requestId, ChatMemory chatMemory) {
    return new MasterAgent(chatClientBuilder, chatMemory);
  }
}
