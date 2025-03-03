package com.minionsai.patient.service;


import com.minionsai.claude.agent.Minion;
import com.minionsai.core.service.AgentManager;
import com.minionsai.patient.agent.MasterAgent;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class PatientAgentManager extends AgentManager {

  public PatientAgentManager(Builder chatClientBuilder) {
    super(chatClientBuilder);
  }

  @Override protected Minion createMasterAgent(String requestId, ChatMemory chatMemory) {
    return new MasterAgent(chatClientBuilder, chatMemory);
  }


}
