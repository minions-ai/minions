package com.minionsai.patient.service;


import com.minionslab.core.domain.Minion;
import com.minionsai.core.service.AgentManager;
import com.minionsai.patient.agent.MasterAgent;
import com.minionslab.core.service.ChatMemoryFactory;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class PatientAgentManager extends AgentManager {

  public PatientAgentManager(Builder chatClientBuilder, ChatMemoryFactory chatMemoryFactory) {
    super(chatClientBuilder, chatMemoryFactory);
  }

  @Override protected Minion createMasterAgent(String requestId, ChatMemory chatMemory) {
    return new MasterAgent(chatClientBuilder, chatMemory);
  }


}
